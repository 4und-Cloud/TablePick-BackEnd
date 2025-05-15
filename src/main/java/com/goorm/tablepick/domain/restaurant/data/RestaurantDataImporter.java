package com.goorm.tablepick.domain.restaurant.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestaurantDataImporter {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tablepickdb";
    private static final String DB_USER = "tablepick";
    private static final String DB_PASSWORD = "tablepick";

    public static void main(String[] args) {
        String csvFilePath = "src/main/resources/google_gangnam_crawling_data.csv";
        Map<String, Long> categoryMap = new HashMap<>();
        Map<String, Long> tagMap = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            loadCategories(conn, categoryMap);
            loadTags(conn, tagMap);

            try (BufferedReader br = Files.newBufferedReader(Paths.get(csvFilePath))) {
                String header = br.readLine();
                String line;
                int lineNum = 1;
                while ((line = br.readLine()) != null) {
                    String[] cols = parseCsvLine(line);
                    if (cols.length < 11) {
                        System.err.println("라인 " + lineNum + "에서 열 개수 부족: " + cols.length);
                        lineNum++;
                        continue;
                    }

                    try {
                        String name = normalizeWhitespace(cols[0].trim());
                        String address = normalizeWhitespace(cols[1].trim());
                        String phone = cols[2].trim();
                        String menuJson = cols[3].trim();
                        String tagsJson = cols[4].trim();
                        String categoryName = cols[5].trim();
                        String imageUrl = cols[6].trim();
                        String hoursJson = cols[7].trim();
                        double latitude = Double.parseDouble(cols[8].trim());
                        double longitude = Double.parseDouble(cols[9].trim());
                        String reviewsJson = cols[10].trim();

                        Long categoryId = categoryMap.get(categoryName);
                        if (categoryId == null) {
                            categoryId = insertCategory(conn, categoryName);
                            if (categoryId != null) {
                                categoryMap.put(categoryName, categoryId);
                            } else {
                                System.err.println("라인 " + lineNum + ": 카테고리 삽입 실패 - " + categoryName);
                                lineNum++;
                                continue;
                            }
                        }

                        long restaurantId = getOrInsertRestaurant(conn, name, categoryId, phone, address, latitude,
                                longitude);
                        if (restaurantId == -1) {
                            System.err.println("라인 " + lineNum + ": 음식점 삽입 실패 - " + name);
                            lineNum++;
                            continue;
                        }

                        insertRestaurantImage(conn, restaurantId, imageUrl);
                        insertOperatingHours(conn, restaurantId, hoursJson);
                        insertMenus(conn, restaurantId, menuJson);
                        insertTagsAndRestaurantTags(conn, restaurantId, tagsJson, tagMap);
                        insertReviews(conn, restaurantId, reviewsJson);

                        conn.commit();
                        System.out.println("라인 " + lineNum + " 처리 완료: " + name);
                    } catch (Exception e) {
                        conn.rollback();
                    }
                    lineNum++;
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("초기화 중 에러: " + e.getMessage());
        }
    }

    private static String[] parseCsvLine(String line) {
        line = line.replaceAll(",\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "\t");
        String[] cols = line.split("\t");
        for (int i = 0; i < cols.length; i++) {
            cols[i] = cols[i].replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
        }
        return cols;
    }

    private static String normalizeWhitespace(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace('\u00A0', ' ')
                .replaceAll("[\\s\\uFEFF]+", " ")
                .trim();
    }


    private static void loadCategories(Connection conn, Map<String, Long> categoryMap) throws SQLException {
        String sql = "SELECT id, name FROM restaurant_category";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categoryMap.put(rs.getString("name"), rs.getLong("id"));
            }
        }
    }

    private static Long insertCategory(Connection conn, String name) throws SQLException {
        String sql = "INSERT IGNORE INTO restaurant_category (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                // 이미 존재하는 경우, ID를 조회
                String selectSql = "SELECT id FROM restaurant_category WHERE name = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setString(1, name);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getLong("id");
                        }
                    }
                }
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    private static long getOrInsertRestaurant(Connection conn, String name, Long categoryId, String phone,
                                              String address, double latitude, double longitude) throws SQLException {

        // 정규화
        name = name.replaceAll("\\s+", " ").trim();
        address = address.replaceAll("\\s+", " ").trim();

        // 1. 중복 체크
        String checkSql = "SELECT id FROM restaurant WHERE name = ? AND address = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, name);
            checkStmt.setString(2, address);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    long existingId = rs.getLong("id");
                    System.out.println("중복 음식점 발견 (기존 ID: " + existingId + "): " + name + " - " + address);
                    return existingId;
                }
            }
        }

        // 2. 중복이 없으면 삽입 시도
        String insertSql = "INSERT INTO restaurant (name, restaurant_category_id, restaurant_phone_number, address, ycoordinate, xcoordinate, max_capacity) VALUES (?, ?, ?, ?, ?, ?, 3)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setLong(2, categoryId);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.setDouble(5, latitude);
            pstmt.setDouble(6, longitude);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("음식점 삽입 실패 (영향받은 행 없음): " + name + " (" + address + ")");
                return -1;
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    System.out.println("새 음식점 삽입 성공 (ID: " + newId + "): " + name + " - " + address);
                    return newId;
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, name);
                    checkStmt.setString(2, address);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            long existingId = rs.getLong("id");
                            System.out.println("중복 음식점 재확인 (기존 ID: " + existingId + "): " + name + " - " + address);
                            return existingId;
                        } else {
                            System.err.println("⚠ 중복인데도 기존 ID 조회 실패: " + name + " - " + address);
                        }
                    }
                } catch (SQLException ex2) {
                    System.err.println("⚠ 중복 처리 중 또 다른 SQL 에러 발생: " + ex2.getMessage());
                }
            }
            System.err.println("음식점 삽입 중 에러: " + e.getMessage() + " - " + name + " (" + address + ")");
            return -1;
        }

        return -1;
    }

    private static void insertRestaurantImage(Connection conn, long restaurantId, String imageUrl) throws SQLException {
        String sql = "INSERT INTO restaurant_image (restaurant_id, image_url) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, restaurantId);
            pstmt.setString(2, imageUrl);
            pstmt.executeUpdate();
        }
    }

    private static final Map<String, String> DAY_MAP = Map.of(
            "월요일", "MONDAY",
            "화요일", "TUESDAY",
            "수요일", "WEDNESDAY",
            "목요일", "THURSDAY",
            "금요일", "FRIDAY",
            "토요일", "SATURDAY",
            "일요일", "SUNDAY"
    );

    private static void insertOperatingHours(Connection conn, long restaurantId, String hoursJson) throws SQLException {
        // 기존 데이터 삭제
        String deleteSql = "DELETE FROM restaurant_operating_hour WHERE restaurant_id = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setLong(1, restaurantId);
            deleteStmt.executeUpdate();
        }

        try {
            JSONObject hoursObj = new JSONObject(hoursJson);
            if (hoursObj.length() == 0) {
                return;
            }

            String sql = "INSERT INTO restaurant_operating_hour (restaurant_id, day_of_week, open_time, close_time, is_holiday) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String day : hoursObj.keySet()) {
                    String engDay = DAY_MAP.getOrDefault(day, null);
                    if (engDay == null) {
                        continue;
                    }

                    JSONArray times = hoursObj.getJSONArray(day);
                    if (times.length() == 0) {
                        continue;
                    }

                    String timeRange = times.getString(0).trim();
                    if (timeRange.equals("휴무일") || timeRange.isEmpty()) {
                        pstmt.setLong(1, restaurantId);
                        pstmt.setString(2, engDay);
                        pstmt.setNull(3, java.sql.Types.TIME);
                        pstmt.setNull(4, java.sql.Types.TIME);
                        pstmt.setBoolean(5, true);
                        pstmt.executeUpdate();
                        continue;
                    }

                    String[] parts = timeRange.split("~");
                    if (parts.length != 2) {
                        continue;
                    }

                    String openTime = convertTo24HourFormat(parts[0].trim());
                    String closeTime = convertTo24HourFormat(parts[1].trim());

                    pstmt.setLong(1, restaurantId);
                    pstmt.setString(2, engDay);
                    pstmt.setString(3, openTime);
                    pstmt.setString(4, closeTime);
                    pstmt.setBoolean(5, false);
                    pstmt.executeUpdate();
                }
            }
        } catch (JSONException e) {
            System.err.println("영업시간 JSON 파싱 실패: " + hoursJson + ", 에러: " + e.getMessage());
        }
    }


    // 오전/오후 시간 문자열을 24시간 HH:mm:ss 포맷으로 변환
    private static String convertTo24HourFormat(String time) {
        time = time.trim();
        boolean isPM = time.startsWith("오후");
        boolean isAM = time.startsWith("오전");
        time = time.replace("오전", "").replace("오후", "").trim();

        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim());

        if (isPM && hour < 12) {
            hour += 12;
        }
        if (isAM && hour == 12) {
            hour = 0;
        }

        return String.format("%02d:%02d:00", hour, minute);
    }


    private static void insertMenus(Connection conn, long restaurantId, String menuJson) throws SQLException {
        try {
            System.out.println("메뉴 JSON 처리 시작: restaurant_id=" + restaurantId + ", menuJson=" + menuJson);
            JSONArray menus = new JSONArray(menuJson);
            if (menus.length() == 0) {
                System.out.println("메뉴 JSON이 비어있음: restaurant_id=" + restaurantId);
                return;
            }
            String sql = "INSERT INTO menu (restaurant_id, name, price) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < menus.length(); i++) {
                    JSONObject menu = menus.getJSONObject(i);
                    if (menu.length() == 0) {
                        System.out.println("빈 메뉴 객체 스킵: restaurant_id=" + restaurantId + ", index=" + i);
                        continue;
                    }
                    String name = menu.getString("menu_name");
                    String priceStr = menu.getString("menu_price").replaceAll("[^0-9]", "");
                    BigDecimal price = new BigDecimal(priceStr).divide(new BigDecimal("100.00"));
                    pstmt.setLong(1, restaurantId);
                    pstmt.setString(2, name);
                    pstmt.setBigDecimal(3, price);
                    try {
                        int affectedRows = pstmt.executeUpdate();
                        System.out.println(
                                "메뉴 삽입 성공: restaurant_id=" + restaurantId + ", name=" + name + ", price=" + price
                                        + ", affectedRows=" + affectedRows);
                    } catch (SQLException e) {
                        if (e.getMessage().contains("Duplicate entry")) {
                            System.out.println("중복 메뉴 스킵: restaurant_id=" + restaurantId + ", name=" + name);
                        } else {
                            throw e;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            System.err.println("메뉴 JSON 파싱 실패: restaurant_id=" + restaurantId + ", menuJson=" + menuJson + ", 에러: "
                    + e.getMessage());
            throw new SQLException("JSON 파싱 실패", e);
        } catch (NumberFormatException e) {
            System.err.println("메뉴 가격 변환 실패: restaurant_id=" + restaurantId + ", menuJson=" + menuJson + ", 에러: "
                    + e.getMessage());
            throw new SQLException("가격 변환 실패", e);
        }
    }

    private static void loadTags(Connection conn, Map<String, Long> tagMap) throws SQLException {
        String sql = "SELECT id, name FROM tag";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tagMap.put(rs.getString("name"), rs.getLong("id"));
            }
        }
    }

    private static Long insertTag(Connection conn, String name) throws SQLException {
        String sql = "INSERT IGNORE INTO tag (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1); // 기본 키 이름 확인 필요
                }
            }
            String selectSql = "SELECT tag_id FROM tag WHERE name = ?"; // tag_id로 수정
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, name);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("tag_id"); // tag_id로 수정
                    }
                }
            }
        }
        return null;
    }

    private static void insertTagsAndRestaurantTags(Connection conn, long restaurantId, String tagsJson,
                                                    Map<String, Long> tagMap) throws SQLException {
        try {
            JSONArray tags = new JSONArray(tagsJson);
            if (tags.length() == 0) {
                return; // 빈 배열일 경우 스킵
            }
            String sql = "INSERT INTO restaurant_tag (restaurant_id, tag_id, count) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < tags.length(); i++) {
                    JSONObject tagObj = tags.getJSONObject(i);
                    if (tagObj.length() == 0) {
                        continue; // 빈 객체일 경우 스킵
                    }
                    String tagName = tagObj.getString("tags");
                    int count = tagObj.getInt("count");

                    Long tagId = tagMap.get(tagName);
                    if (tagId == null) {
                        tagId = insertTag(conn, tagName);
                        if (tagId != null) {
                            tagMap.put(tagName, tagId);
                        } else {
                            System.err.println("태그 삽입 실패: " + tagName);
                            continue;
                        }
                    }

                    pstmt.setLong(1, restaurantId);
                    pstmt.setLong(2, tagId);
                    pstmt.setInt(3, count);
                    pstmt.executeUpdate();
                }
            }
        } catch (JSONException e) {
            System.err.println("태그 JSON 파싱 실패: " + tagsJson + ", 에러: " + e.getMessage());
        }
    }

    private static void insertReviews(Connection conn, long restaurantId, String reviewsJson) throws SQLException {
        try {
            // 1. 빈 데이터 처리
            if (reviewsJson == null || reviewsJson.trim().isEmpty() || reviewsJson.trim().equals("[]")) {
                System.out.println("리뷰 데이터가 비어있습니다. 스킵합니다.");
                return;
            }

            // 2. JSON 정제
            reviewsJson = reviewsJson
                    .replaceAll("\\\\(?![\"ntr])", " ") // 잘못된 백슬래시 제거
                    .replaceAll("[\\r\\n]+", " ") // 줄바꿈을 공백으로 변환
                    .replaceAll("(?<!\\\\)\"", "\\\\\"") // 이스케이프되지 않은 큰따옴표 이스케이프 처리
                    .replaceAll("(?<!\\\\)'", "\"") // 작은따옴표를 큰따옴표로 변환
                    .replaceAll("\\s+", " ") // 불필요한 공백 정리
                    .trim();

            // 3. JSON 파싱
            JSONArray reviews;
            try {
                reviews = new JSONArray(reviewsJson);
            } catch (JSONException e) {
                // JSON 파싱 실패 시 배열로 감싸서 재시도
                try {
                    reviews = new JSONArray("[" + reviewsJson + "]");
                } catch (JSONException e2) {
                    System.err.println(
                            "리뷰 JSON 파싱 실패 (재시도 실패): " + reviewsJson.substring(0, Math.min(100, reviewsJson.length()))
                                    + "... (길이: " + reviewsJson.length() + "), 에러: " + e2.getMessage());
                    return;
                }
            }

            // 4. 빈 배열 체크
            if (reviews.length() == 0) {
                System.out.println("리뷰 배열이 비어있습니다. 스킵합니다.");
                return;
            }

            // 5. 리뷰 삽입 준비
            String sqlMember = "INSERT INTO member (nickname, email) VALUES (?, ?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)";
            String sqlReview = "INSERT INTO board (restaurant_id, member_id, content) VALUES (?, ?, ?)";
            String sqlImage = "INSERT INTO board_image (board_id, image_url) VALUES (?, ?)";
            try (PreparedStatement pstmtMember = conn.prepareStatement(sqlMember, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmtReview = conn.prepareStatement(sqlReview, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmtImage = conn.prepareStatement(sqlImage)) {

                // 6. 각 리뷰를 개별적으로 처리
                for (int i = 0; i < reviews.length(); i++) {
                    try {
                        Object item = reviews.get(i);
                        if (!(item instanceof JSONObject)) {
                            System.err.println("리뷰 " + i + "은(는) JSONObject가 아님: " + item.toString());
                            continue;
                        }

                        JSONObject review = (JSONObject) item;
                        if (review.length() == 0) {
                            continue;
                        }

                        String reviewer = review.optString("reviewer", "알 수 없는 사용자");
                        String content = review.optString("content", "");
                        JSONArray photos = review.optJSONArray("photo");
                        if (photos == null) {
                            photos = new JSONArray();
                        }

                        pstmtMember.setString(1, reviewer);
                        pstmtMember.setString(2, reviewer.replaceAll("[^a-zA-Z0-9]", "") + "@mock.com");
                        pstmtMember.executeUpdate();
                        long memberId;
                        try (ResultSet rs = pstmtMember.getGeneratedKeys()) {
                            if (rs.next()) {
                                memberId = rs.getLong(1);
                            } else {
                                continue;
                            }
                        }

                        pstmtReview.setLong(1, restaurantId);
                        pstmtReview.setLong(2, memberId);
                        pstmtReview.setString(3, content);
                        pstmtReview.executeUpdate();

                        long reviewId;
                        try (ResultSet rs = pstmtReview.getGeneratedKeys()) {
                            if (rs.next()) {
                                reviewId = rs.getLong(1);
                            } else {
                                continue;
                            }
                        }

                        for (int j = 0; j < photos.length(); j++) {
                            String photoUrl = photos.getString(j);
                            pstmtImage.setLong(1, reviewId);
                            pstmtImage.setString(2, photoUrl);
                            pstmtImage.executeUpdate();
                        }
                    } catch (JSONException e) {
                        System.err.println("리뷰 " + i + " 파싱 실패: " + reviews.get(i).toString()
                                .substring(0, Math.min(100, reviews.get(i).toString().length())) + "... 에러: "
                                + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("리뷰 처리 중 예상치 못한 에러: " + e.getMessage());
        }
    }
}
