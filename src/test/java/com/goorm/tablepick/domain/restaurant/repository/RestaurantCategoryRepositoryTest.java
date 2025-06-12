//package com.goorm.tablepick.domain.restaurant.repository;
//
//import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ActiveProfiles;
//
//@ActiveProfiles("test")
//@DataJpaTest
//public class RestaurantCategoryRepositoryTest {
//    @Autowired
//    private RestaurantCategoryRepository restaurantCategoryRepository;
//
//    @DisplayName("이름으로 식당 카테고리의 존재 여부를 찾는다.")
//    @Test
//    void findByName(){
//    // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder()
//                .name("한식")
//                .build();
//        restaurantCategoryRepository.save(restaurantCategory1);
//
//    // when
//        boolean existing = restaurantCategoryRepository.existsByName(restaurantCategory1.getName());
//
//    // then
//        Assertions.assertTrue(existing);
//    }
//
//    @DisplayName("다른 이름으로 식당 카테고리의 존재 여부를 찾는다.")
//    @Test
//    void findByDifferentName(){
//        // given
//        RestaurantCategory restaurantCategory1 = RestaurantCategory.builder()
//                .name("한식")
//                .build();
//        restaurantCategoryRepository.save(restaurantCategory1);
//
//        // when
//        boolean existing = restaurantCategoryRepository.existsByName("중식");
//
//        // then
//        Assertions.assertFalse(existing);
//    }
//}
