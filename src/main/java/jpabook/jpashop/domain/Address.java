package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * 값 타입은 기본적으로 생성 후 변경 불가능하게 설계해야 한다.
 * 생성자에서 값을 모두 초기화하고, 변경 불가능한 클래스로 만들자.
 */
@Embeddable
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    /**
     * JPA 구현 라이브러리가 값 타입을 활용해 리플랙션, 프록시와 같은 기술을 사용할 수 있도록
     * 기본 생성자를 만들어 줘야 한다.
     */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(getCity(), address.getCity()) && Objects.equals(getStreet(), address.getStreet()) && Objects.equals(getZipcode(), address.getZipcode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCity(), getStreet(), getZipcode());
    }
}
