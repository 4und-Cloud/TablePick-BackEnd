package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.restaurant.entity.Menu;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import com.goorm.tablepick.domain.restaurant.repository.MenuRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantCategoryRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantImageRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantOperatingHourRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RestaurantCloneService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository categoryRepository;
    private final RestaurantImageRepository imageRepository;
    private final MenuRepository menuRepository;
    private final RestaurantOperatingHourRepository operatingHourRepository;
    private final EntityManager entityManager;

    private static final int PAGE_SIZE = 100;
    private static final int MAX_NAME_VARIANTS = 5;
    private static final int MAX_SHUFFLE_ATTEMPTS = 100;

    public void cloneRestaurantData() {
        int page = 3505;
        while (true) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            List<Restaurant> originals = restaurantRepository.findAll(pageable).getContent();
            if (originals.isEmpty()) break;

            for (Restaurant original : originals) {
                try {
                    cloneVariants(original);
                } catch (Exception e) {
                    System.err.println("레스토랑 복제 실패: " + original.getId() + " - " + e.getMessage());
                }
            }

            page++;
            entityManager.clear();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void cloneVariants(Restaurant original) {
        String name = original.getName();
        if (name == null || name.isBlank()) return;

        // 카테고리 복제
        RestaurantCategory originalCat = original.getRestaurantCategory();
        RestaurantCategory clonedCat = RestaurantCategory.builder()
                .name(originalCat.getName())
                .build();
        categoryRepository.save(clonedCat);

        // 이름 변형 생성
        List<String> chars = new ArrayList<>();
        for (char c : name.toCharArray()) chars.add(String.valueOf(c));

        Set<String> variants = new HashSet<>();
        int attempts = 0;
        while (variants.size() < MAX_NAME_VARIANTS && attempts < MAX_SHUFFLE_ATTEMPTS) {
            Collections.shuffle(chars);
            variants.add(String.join("", chars));
            attempts++;
        }

        for (String variant : variants) {
            if (restaurantRepository.existsByName(variant)) continue;

            // 레스토랑 복제
            Restaurant clone = Restaurant.builder()
                    .name(variant)
                    .address(original.getAddress())
                    .restaurantCategory(clonedCat)
                    .maxCapacity(original.getMaxCapacity())
                    .xcoordinate(original.getXcoordinate())
                    .ycoordinate(original.getYcoordinate())
                    .restaurantPhoneNumber(original.getRestaurantPhoneNumber())
                    .build();

            restaurantRepository.save(clone);

            // 연관 데이터 복제
            cloneImages(original, clone);
            cloneMenus(original, clone);
            cloneOperatingHours(original, clone);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void cloneImages(Restaurant original, Restaurant clone) {
        List<RestaurantImage> images = imageRepository.findByRestaurantId(original.getId());
        for (RestaurantImage img : images) {
            RestaurantImage copy = RestaurantImage.builder()
                    .imageUrl(img.getImageUrl())
                    .restaurant(clone)
                    .build();
            imageRepository.save(copy);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void cloneMenus(Restaurant original, Restaurant clone) {
        List<Menu> menus = menuRepository.findByRestaurantId(original.getId());
        for (Menu menu : menus) {
            Menu copy = Menu.builder()
                    .name(menu.getName())
                    .price(menu.getPrice())
                    .restaurant(clone)
                    .build();
            menuRepository.save(copy);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void cloneOperatingHours(Restaurant original, Restaurant clone) {
        List<RestaurantOperatingHour> hours = operatingHourRepository.findByRestaurantId(original.getId());
        for (RestaurantOperatingHour hour : hours) {
            RestaurantOperatingHour copy = RestaurantOperatingHour.builder()
                    .dayOfWeek(hour.getDayOfWeek())
                    .openTime(hour.getOpenTime())
                    .closeTime(hour.getCloseTime())
                    .isHoliday(hour.isHoliday())
                    .restaurant(clone)
                    .build();
            operatingHourRepository.save(copy);
        }
    }
}