package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderQueryService orderQueryService;

    /**
     * 엔티티 직접 반환은 그냥 쓰지 말자.
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());

        /**
         * 단순히 이름을 가져오는 것이 아닌 Member, Delivery, I tem 객체를 사용함으로써
         * 객체 그래프에 필요한 엔티티들을 초기화 한다.
         */
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }

        return orders;
    }

    /**
     * 엔티티를 그대로 노출하지 않고 API 스펙에 맞는 DTO로 변환해서 반환한다.
     * 하지만 아직 N+1 문제가 해결되지 않았다.
     *
     * @return
     */
    @GetMapping("/api/v2/orders")
    public List<OrderQueryService.OrderDto> orderV2() {
        return orderQueryService.orderV2();
    }

    /**
     * fetch join 전략과 distinct를 사용해 N+1 문제를 해결했다.
     * 하지만 일대다 매핑을 fetch join 할 경우 치명적인 문제가 있다.
     * <p>
     * 1. 페이징 쿼리가 불가능하다는 점이다.
     * 애플리케이션 차원에서 distinct가 이루어져 원하는 결과가 보여지지만
     * db차원에서는 페이징이 불가능하다.
     * <p>
     * 결국 하이버네이트가 경고 로그를 남기고 메모리에 검색 결과를 퍼올린 후 페이징을 시도한다.
     * out of memory 발생 가능성이 높다는 의미이다.
     * <p>
     * 2. 컬렉션 fetch join 전략은 1개만 사용 할 수 있다.
     * 컬렉션 여러개가 fetch join되면 데이터가 예측할 수 없이 증가한다.
     * 위와 같이 데이터가 부정합하게 조회될 수 있다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        return orderRepository.findAllWithItem(new OrderSearch()).stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * 쿼리 호출수가 1+N -> 1+1로 최적화된다.
     * 조인보다 DB 데이터 전송량이 최적화 된다.
     * (Order와 OrderItem을 조인하면 Order가 OrderItem 만큼 중복해서 조회된다. 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없다.)
     * 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만, DB 데이터 전송량이 감소한다.
     * 컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능하다.
     * <p>
     * ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다. 따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄이고 해결하고,
     * 나머지는 hibernate.default_batch_fetch_size 로 최적화 하자.
     * <p>
     * 팁)
     * 기본적으로 batchSize를 100으로 해놓고, DB가 순간 부하를 어디까지 견딜 수 있는
     * 판단하면 늘리던지 줄이던지 결정한다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> pagingOrderV3(
            @RequestParam(value = "offset", defaultValue = "1") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDtoOptimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDtoFlat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(o -> new OrderItemDto(o))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private Long id;
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.id = orderItem.getItem().getId();
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
