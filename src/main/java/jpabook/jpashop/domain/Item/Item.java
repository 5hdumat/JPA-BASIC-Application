package jpabook.jpashop.domain.Item;

import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {
    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    private String name;
    private int price;
    private int StockQuantity;

    /**
     * 비즈니스 로직
     * 엔티티가 스스로 해결 할 수 있는 비즈니스 로직은 엔티티 자체에서 해결하는 게
     * 객체지향의 관점에서 응집도가 좋아진다.
     */
    public void addStock(int quantity) {
        this.StockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int restStock = this.StockQuantity - quantity;

        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }

        this.StockQuantity = restStock;
    }

    public void changeItem(String name, int price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.StockQuantity = stockQuantity;
    }
}
