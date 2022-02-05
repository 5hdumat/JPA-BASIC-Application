package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Item.Book;
import jpabook.jpashop.domain.Item.Item;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "새절", "123456"));
        em.persist(member);

        Book book = new Book();
        book.setAuthor("공책");
        book.setIsbn("181727232");
        book.setStockQuantity(100);
        book.setPrice(2000);
        em.persist(book);

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), 10);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문 시 상태는 ORDER여야 한다.");
        assertEquals(1, getOrder.getOrderItems().size(), "주문 상품 종류 수가 정확해야 한다.");
        assertEquals(20000, getOrder.getTotalPrice(), "가격은 가격 * 수량이다.");
        assertEquals(90, book.getStockQuantity(), "주문 수량 만큼 재고가 줄어야 한다.");
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "새절", "123456"));
        em.persist(member);

        Book book = new Book();
        book.setAuthor("공책");
        book.setIsbn("181727232");
        book.setStockQuantity(100);
        book.setPrice(2000);
        em.persist(book);

        // when

        // then
        assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), 200));
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "새절", "123456"));
        em.persist(member);

        Book book = new Book();
        book.setAuthor("공책");
        book.setIsbn("181727232");
        book.setStockQuantity(100);
        book.setPrice(2000);
        em.persist(book);

        Long orderId = orderService.order(member.getId(), book.getId(), 10);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소 상태여야 한다.");
        assertEquals(100, book.getStockQuantity(), "주문 취소 상품은 재고가 그만큼 증가해야 한다.");
    }
}