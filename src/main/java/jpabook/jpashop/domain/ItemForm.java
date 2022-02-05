package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ItemForm {
    private Long id;

    @NotEmpty(message = "상품명을 입력해주세요.")
    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String isbn;
}
