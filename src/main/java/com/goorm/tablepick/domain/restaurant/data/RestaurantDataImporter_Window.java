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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestaurantDataImporter_Window {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/tablepickdb";
    private static final String DB_USER = "tablepick";
    private static final String DB_PASSWORD = "tablepick";
    private static final List<String> DAYS_OF_WEEK = Arrays.asList(
            "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"
    );

    public static void main(String[] args) {
        String csvFilePath = "src/main/resources/gangnam_restaurants_cleaned_data.csv";
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
                int processedRestaurants = 0;

                while ((line = br.readLine()) != null) {
                    String[] cols = parseCsvLine(line);
                    if (cols.length < 10) {
                        System.err.println("라인 " + lineNum + "에서 열 개수 부족: " + cols.length);
                        lineNum++;
                        continue;
                    }

                    try {
                        String name = normalizeWhitespace(cols[0].trim());
                        String imageUrl = normalizeWhitespace(cols[1].trim());
                        String address = normalizeWhitespace(cols[2].trim());
                        String categoryName = cols[3].trim();
                        String phone = cols[4].trim();
                        String hoursJson = cols[5].trim();
                        String menuJson = cols[6].trim();
                        String reviewsJson = cols[7].trim();
                        double latitude = Double.parseDouble(cols[8].trim());
                        double longitude = Double.parseDouble(cols[9].trim());

                        Long categoryId = categoryMap.get(categoryName);
                        if (categoryId == null) {
                            categoryId = insertCategory(conn, categoryName);
                            if (categoryId != null) {
                                categoryMap.put(categoryName, categoryId);
                            } else {
                                System.err.println(
                                        "라인 " + lineNum + ": 카테고리 삽입 실패 - " + categoryName + ", 기본 카테고리 사용 시도");
                                categoryId = insertDefaultCategory(conn);
                                if (categoryId != null) {
                                    categoryMap.put(categoryName, categoryId);
                                    categoryMap.put("기타", categoryId);
                                } else {
                                    System.err.println("라인 " + lineNum + ": 기본 카테고리 삽입도 실패 - " + name + ", 스킵");
                                    lineNum++;
                                    continue;
                                }
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
                        insertReviews(conn, restaurantId, reviewsJson);

                        conn.commit();
                        System.out.println("라인 " + lineNum + " 처리 완료: " + name + ", restaurant_id=" + restaurantId);
                        processedRestaurants++;
                    } catch (Exception e) {
                        System.err.println("라인 " + lineNum + " 처리 중 에러: " + e.getMessage());
                        conn.rollback();
                    }
                    lineNum++;
                }
                System.out.println("총 처리된 식당 수: " + processedRestaurants);
                if (processedRestaurants != 467) {
                    System.err.println("처리된 식당 수가 467개가 아님: " + processedRestaurants);
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("초기화 중 에러: " + e.getMessage());
        }
    }

    private static String[] parseCsvLine(String line) {
        // 복잡한 lookahead 대신, 큰따옴표 내부가 아닌 쉼표만 탭으로 대체 (단순화 버전)
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            }
            if (c == ',' && !inQuotes) {
                sb.append('\t');
            } else {
                sb.append(c);
            }
        }

        String[] cols = sb.toString().split("\t");

        for (int i = 0; i < cols.length; i++) {
            cols[i] = cols[i]
                    .replaceAll("^\"|\"$", "")
                    .replaceAll("^'|'$", "")
                    .trim(); // 공백도 제거
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
        if (name == null || name.trim().isEmpty()) {
            return insertDefaultCategory(conn);
        }

        Long existingId = getCategoryIdByName(conn, name);
        if (existingId != null) {
            return existingId;
        }

        String sql = "INSERT INTO restaurant_category (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("카테고리 삽입 실패 (영향받은 행 없음): " + name);
                return null;
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("카테고리 삽입 중 에러: " + name + ", 에러: " + e.getMessage());
        }

        return getCategoryIdByName(conn, name);
    }

    private static Long insertDefaultCategory(Connection conn) throws SQLException {
        String defaultCategory = "기타";
        Long existingId = getCategoryIdByName(conn, defaultCategory);
        if (existingId != null) {
            return existingId;
        }

        String sql = "INSERT INTO restaurant_category (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, defaultCategory);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("기본 카테고리 삽입 실패 (영향받은 행 없음): " + defaultCategory);
                return null;
            }
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("기본 카테고리 삽입 성공: " + defaultCategory + ", ID: " + rs.getLong(1));
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("기본 카테고리 삽입 중 에러: " + defaultCategory + ", 에러: " + e.getMessage());
            return null;
        }
        return null;
    }

    private static Long getCategoryIdByName(Connection conn, String name) throws SQLException {
        String selectSql = "SELECT id FROM restaurant_category WHERE name = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }

    private static long getOrInsertRestaurant(Connection conn, String name, Long categoryId, String phone,
                                              String address, double latitude, double longitude) throws SQLException {
        name = name.replaceAll("\\s+", " ").trim();
        address = address.replaceAll("\\s+", " ").trim();

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
                        }
                    }
                }
            }
            System.err.println("음식점 삽입 중 에러: " + e.getMessage() + " - " + name + " (" + address + ")");
            throw e;
        }
        return -1;
    }

    private static void insertRestaurantImage(Connection conn, long restaurantId, String imageUrl) throws SQLException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO restaurant_image (restaurant_id, image_url) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, restaurantId);
            pstmt.setString(2, imageUrl);
            pstmt.executeUpdate();
            System.out.println("이미지 삽입 성공: restaurant_id=" + restaurantId + ", image_url=" + imageUrl);
        } catch (SQLException e) {
            System.err.println("이미지 삽입 실패: restaurant_id=" + restaurantId + ", 에러: " + e.getMessage());
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
        System.out.println(
                "영업시간 JSON 처리 시작: restaurant_id=" + restaurantId + ", hoursJson=" + (hoursJson.length() > 100 ?
                        hoursJson.substring(0, 100) + "..." : hoursJson));

        String deleteSql = "DELETE FROM restaurant_operating_hour WHERE restaurant_id = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setLong(1, restaurantId);
            deleteStmt.executeUpdate();
        }

        JSONObject hoursObj;
        if (hoursJson == null || hoursJson.trim().isEmpty() || hoursJson.trim().equals("{}")) {
            System.out.println("영업시간 데이터 없음, 모든 요일을 휴무일로 설정: restaurant_id=" + restaurantId);
            hoursObj = new JSONObject();
        } else {
            hoursObj = parseJsonObject(hoursJson);
        }

        String sql = "INSERT INTO restaurant_operating_hour (restaurant_id, day_of_week, open_time, close_time, is_holiday) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int insertedRows = 0;
            for (String day : DAYS_OF_WEEK) {
                String engDay = DAY_MAP.get(day);
                if (engDay == null) {
                    System.err.println("알 수 없는 요일: " + day + ", restaurant_id=" + restaurantId);
                    continue;
                }

                String timeRange = hoursObj.optString(day, "휴무일").trim();
                if (timeRange.equals("휴무일") || timeRange.isEmpty()) {
                    pstmt.setLong(1, restaurantId);
                    pstmt.setString(2, engDay);
                    pstmt.setNull(3, java.sql.Types.TIME);
                    pstmt.setNull(4, java.sql.Types.TIME);
                    pstmt.setBoolean(5, true);
                    pstmt.executeUpdate();
                    System.out.println("휴무일 삽입: restaurant_id=" + restaurantId + ", day=" + engDay);
                    insertedRows++;
                    continue;
                }

                String[] parts = timeRange.split("~");
                if (parts.length != 2) {
                    System.err.println(
                            "잘못된 시간 형식, 휴무일로 설정: " + timeRange + ", day=" + engDay + ", restaurant_id=" + restaurantId);
                    pstmt.setLong(1, restaurantId);
                    pstmt.setString(2, engDay);
                    pstmt.setNull(3, java.sql.Types.TIME);
                    pstmt.setNull(4, java.sql.Types.TIME);
                    pstmt.setBoolean(5, true);
                    pstmt.executeUpdate();
                    System.out.println("휴무일 삽입 (잘못된 형식): restaurant_id=" + restaurantId + ", day=" + engDay);
                    insertedRows++;
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
                System.out.println("영업시간 삽입: restaurant_id=" + restaurantId + ", day=" + engDay + ", " + openTime + "~"
                        + closeTime);
                insertedRows++;
            }
            if (insertedRows != 7) {
                System.err.println("영업시간 레코드 수가 7개가 아님: restaurant_id=" + restaurantId + ", 삽입된 레코드 수=" + insertedRows);
            } else {
                System.out.println("영업시간 처리 완료: restaurant_id=" + restaurantId + ", 총 " + insertedRows + "개 레코드 삽입");
            }
        } catch (SQLException e) {
            System.err.println("영업시간 삽입 실패: restaurant_id=" + restaurantId + ", 에러: " + e.getMessage());
            throw e;
        }
    }

    private static String convertTo24HourFormat(String time) {
        try {
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
        } catch (Exception e) {
            System.err.println("시간 변환 실패: " + time + ", 에러: " + e.getMessage());
            return "00:00:00";
        }
    }

    private static Long insertTag(Connection conn, String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String selectSql = "SELECT id FROM tag WHERE name = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        String insertSql = "INSERT INTO tag (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            return getTagIdByName(conn, name);
        }

        return null;
    }

    private static Long getTagIdByName(Connection conn, String name) throws SQLException {
        String selectSql = "SELECT id FROM tag WHERE name = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, name);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }

    private static void insertMenus(Connection conn, long restaurantId, String menuJson) throws SQLException {
        System.out.println("메뉴 JSON 처리 시작: restaurant_id=" + restaurantId + ", menuJson=" + (menuJson.length() > 100 ?
                menuJson.substring(0, 100) + "..." : menuJson));
        JSONArray menus = parseJsonArray(menuJson);
        if (menus.length() == 0) {
            System.out.println("메뉴 JSON이 비어있음: restaurant_id=" + restaurantId);
            return;
        }

        String sql = "INSERT INTO menu (restaurant_id, name, price) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE price=VALUES(price)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < menus.length(); i++) {
                JSONObject menu = menus.optJSONObject(i);
                if (menu == null || menu.length() == 0) {
                    System.out.println("빈 메뉴 객체 스킵: restaurant_id=" + restaurantId + ", index=" + i);
                    continue;
                }
                String name = menu.optString("메뉴명", "Unnamed");
                String priceStr = menu.optString("가격", "0").replaceAll("[^0-9]", "");
                BigDecimal price = new BigDecimal(priceStr.isEmpty() ? "0" : priceStr).divide(new BigDecimal("1"));
                pstmt.setLong(1, restaurantId);
                pstmt.setString(2, name);
                pstmt.setBigDecimal(3, price);
                try {
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println("메뉴 삽입 성공: restaurant_id=" + restaurantId + ", name=" + name + ", price=" + price
                            + ", affectedRows=" + affectedRows);
                } catch (SQLException e) {
                    System.err.println(
                            "메뉴 삽입 실패: restaurant_id=" + restaurantId + ", name=" + name + ", 에러: " + e.getMessage());
                }
            }
        }
    }

    private static JSONObject parseJsonObject(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return new JSONObject();
            }
            json = json.replaceAll("(?<!\\\\)\"\"", "\"").replaceAll("\\\\(?![\"ntr])", "");
            return new JSONObject(json);
        } catch (JSONException e) {
            System.err.println(
                    "JSON 파싱 실패, 빈 객체 반환: " + (json != null && json.length() > 100 ? json.substring(0, 100) + "..."
                            : json) + ", 에러: " + e.getMessage());
            return new JSONObject();
        }
    }

    private static JSONArray parseJsonArray(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return new JSONArray();
            }
            json = json.replaceAll("(?<!\\\\)\"\"", "\"").replaceAll("\\\\(?![\"ntr])", "");
            return new JSONArray(json);
        } catch (JSONException e) {
            System.err.println(
                    "JSON 배열 파싱 실패, 빈 배열 반환: " + (json != null && json.length() > 100 ? json.substring(0, 100) + "..."
                            : json) + ", 에러: " + e.getMessage());
            return new JSONArray();
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

    private static void insertReviews(Connection conn, long restaurantId, String reviewsJson) throws SQLException {
        System.out.println(
                "리뷰 JSON 처리 시작: restaurant_id=" + restaurantId + ", reviewsJson=" + (reviewsJson.length() > 100 ?
                        reviewsJson.substring(0, 100) + "..." : reviewsJson));
        if (reviewsJson == null || reviewsJson.trim().isEmpty() || reviewsJson.trim().equals("[]")) {
            System.out.println("리뷰 데이터가 비어있습니다. 스킵합니다.: restaurant_id=" + restaurantId);
            return;
        }

        JSONArray reviews = parseJsonArray(reviewsJson);
        if (reviews.length() == 0) {
            System.out.println("리뷰 배열이 비어있습니다. 스킵합니다.: restaurant_id=" + restaurantId);
            return;
        }

        long memberId = getOrCreateAnonymousMember(conn);

        String sqlBoard = "INSERT INTO board (restaurant_id, member_id, content, created_at) VALUES (?, ?, ?, ?)";
        String sqlBoardImage = "INSERT INTO board_image (board_id, image_url) VALUES (?, ?)";
        String sqlBoardKeyword = "INSERT INTO board_keyword (board_id, keyword) VALUES (?, ?)";
        String sqlBoardTag = "INSERT INTO board_tag (board_id, tag_id, restaurant_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmtBoard = conn.prepareStatement(sqlBoard, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtBoardImage = conn.prepareStatement(sqlBoardImage);
             PreparedStatement pstmtBoardKeyword = conn.prepareStatement(sqlBoardKeyword);
             PreparedStatement pstmtBoardTag = conn.prepareStatement(sqlBoardTag)) {

            for (int i = 0; i < reviews.length(); i++) {
                try {
                    JSONObject review = reviews.optJSONObject(i);
                    if (review == null || review.length() == 0) {
                        System.out.println("빈 리뷰 객체 스킵: restaurant_id=" + restaurantId + ", index=" + i);
                        continue;
                    }

                    String content = review.optString("게시글", "");
                    JSONArray photos = review.optJSONArray("이미지");
                    JSONArray keywords = review.optJSONArray("키워드");
                    JSONArray tags = review.optJSONArray("태그");
                    String createdAtStr = review.optString("작성시간", "2025-05-29 12:00:00");
                    Timestamp createdAt = Timestamp.valueOf(createdAtStr.replace("T", " "));

                    if (photos == null) {
                        photos = new JSONArray();
                    }
                    if (keywords == null) {
                        keywords = new JSONArray();
                    }
                    if (tags == null) {
                        tags = new JSONArray();
                    }

                    pstmtBoard.setLong(1, restaurantId);
                    pstmtBoard.setLong(2, memberId);
                    pstmtBoard.setString(3, content);
                    pstmtBoard.setTimestamp(4, createdAt);
                    pstmtBoard.executeUpdate();

                    long boardId;
                    try (ResultSet rs = pstmtBoard.getGeneratedKeys()) {
                        if (rs.next()) {
                            boardId = rs.getLong(1);
                        } else {
                            System.err.println("보드 ID 생성 실패: restaurant_id=" + restaurantId + ", index=" + i);
                            continue;
                        }
                    }

                    for (int j = 0; j < photos.length(); j++) {
                        String photoUrl = photos.optString(j, "");
                        if (photoUrl.isEmpty()) {
                            continue;
                        }
                        pstmtBoardImage.setLong(1, boardId);
                        pstmtBoardImage.setString(2, photoUrl);
                        pstmtBoardImage.executeUpdate();
                        System.out.println("보드 이미지 삽입: board_id=" + boardId + ", photoUrl=" + photoUrl);
                    }

                    for (int j = 0; j < keywords.length(); j++) {
                        String keyword = keywords.optString(j, "");
                        if (keyword.isEmpty()) {
                            continue;
                        }
                        pstmtBoardKeyword.setLong(1, boardId);
                        pstmtBoardKeyword.setString(2, keyword);
                        pstmtBoardKeyword.executeUpdate();
                        System.out.println("보드 키워드 삽입: board_id=" + boardId + ", keyword=" + keyword);
                    }

                    for (int j = 0; j < tags.length(); j++) {
                        String tagName = tags.optString(j, "").trim();
                        if (tagName == null || tagName.isEmpty()) {
                            System.out.println("빈 태그 이름 스킵: restaurant_id=" + restaurantId + ", index=" + j);
                            continue;
                        }

                        Long tagId = insertTag(conn, tagName);
                        if (tagId == null) {
                            System.err.println("태그 ID 생성 실패: tagName=" + tagName + ", restaurant_id=" + restaurantId);
                            continue;
                        }
                        System.out.println(
                                "태그 ID 생성 성공: tagName=" + tagName + ", tagId=" + tagId + ", board_id=" + boardId);

                        pstmtBoardTag.setLong(1, boardId);
                        pstmtBoardTag.setLong(2, tagId);
                        pstmtBoardTag.setLong(3, restaurantId);
                        pstmtBoardTag.executeUpdate();
                        System.out.println("보드 태그 삽입: board_id=" + boardId + ", tag=" + tagName + ", tag_id=" + tagId
                                + ", restaurant_id=" + restaurantId);
                    }
                    System.out.println("리뷰 처리 성공: restaurant_id=" + restaurantId + ", board_id=" + boardId);
                } catch (JSONException e) {
                    System.err.println("리뷰 " + i + " 파싱 실패: restaurant_id=" + restaurantId + ", 에러: " + e.getMessage());
                } catch (SQLException e) {
                    System.err.println("리뷰 " + i + " 삽입 실패: restaurant_id=" + restaurantId + ", 에러: " + e.getMessage());
                    throw e;
                }
            }
        }
    }

    private static long getOrCreateAnonymousMember(Connection conn) throws SQLException {
        String selectSql = "SELECT id FROM member WHERE nickname = ? AND email = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, "익명");
            selectStmt.setString(2, "anonymous@mock.com");
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        String insertSql = "INSERT INTO member " +
                "(nickname, email, gender, birthdate, phone_number, is_member_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "익명");
            pstmt.setString(2, "anonymous@mock.com");
            pstmt.setString(3, "MALE");
            pstmt.setObject(4, LocalDate.of(1970, 1, 1));
            pstmt.setString(5, "010-0000-0000");
            pstmt.setBoolean(6, false);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, "익명");
            selectStmt.setString(2, "anonymous@mock.com");
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        throw new SQLException("익명 멤버 생성 또는 조회 실패");
    }
}
