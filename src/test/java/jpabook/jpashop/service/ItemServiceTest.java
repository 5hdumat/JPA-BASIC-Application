package jpabook.jpashop.service;

import jpabook.jpashop.domain.Item.Book;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemServiceTest {
    @Autowired
    EntityManager em;

    @Test
    public void 상품수정() throws Exception {
        //given
        Book book = em.find(Book.class, 1L);

        // when
        book.setName("책 이름 변경");

        // then
    }
}