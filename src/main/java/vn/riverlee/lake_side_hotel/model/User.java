package vn.riverlee.lake_side_hotel.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Entity(name = "User")
@Table(name = "tbl_user")
public class User extends AbstractEntity {
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Booking> bookings;
}
