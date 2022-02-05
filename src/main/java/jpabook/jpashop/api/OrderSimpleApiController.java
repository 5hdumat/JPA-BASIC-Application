package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XToOne(ManyToOne, OneToOne) 관계 성능 최적화
 * <p>
 * ORDER
 * ORDER -> MEMBER
 * ORDER -> DELIVERY
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        return orderRepository.findAll(new OrderSearch());
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        return orderRepository.findAll(new OrderSearch()).stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        return orderRepository.findAllWithMemberDelivery()
                .stream().map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * 1. SELECT 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트워크 용량 최적화 (생각보다 미비)
     * 2. 리포지토리 재사용성 떨어짐 (API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점)
     * 리포지토리는 객체 그래프 조회와 같은 용도로 사용해야 한다. 아래와 같이 Dto로 API 스펙에 맞춘 코드를 리포지토리에
     * 구현해버리면, 물리적으로는 분리되어 있지만, 논리적으로는 분리된 형태가 깨지는 형태가 된다.
     * <p>
     * -> 가급적 Repository는 순수한 엔티티만 조회할 수 있도록 하고,
     * 화면에 종속적인 쿼리들은 Simplequery 패키지를 따로 두어 관리하는게 유지보수에 좋다.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
        }
    }

}
