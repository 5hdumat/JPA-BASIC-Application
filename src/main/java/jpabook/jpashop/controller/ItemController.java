package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Item.Book;
import jpabook.jpashop.domain.Item.Item;
import jpabook.jpashop.domain.ItemForm;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("itemForm", new ItemForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(@Valid ItemForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "items/createItemForm";
        }

        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String items(Model model) {
        List<Item> items = itemService.findAll();
        model.addAttribute("items", items);
        return "/items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        ItemForm form = new ItemForm();
        form.setId(itemId);
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("itemForm", form);
        return "items/updateItemForm";
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @Valid ItemForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "items/updateItemForm";
        }


        /**
         * [데이터 변경 주의사항]
         *
         * - 컨트롤러에서 어설프게 엔티티를 생성하지 말자.
         * - 트랜잭션이 있는 서비스 계층에 식별자와 변경할 데이터를 '명확하게' 전달해야 한다.
         * - 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경해야한다.
         * - 변경해야 할 데이터가 너무 많다면 DTO를 생성하는 것도 좋은 방법이다.
         * - merge()는 엔티티의 모든 필드를 갈아끼우기 때문에 사용하지 않는게 좋다.
         * - setter()를 남발하지말고 엔티티에 비즈니스 로직이 담겨있는 메서드를 생성하는게 유지보수에 좋다.
         */
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
