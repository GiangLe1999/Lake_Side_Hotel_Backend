package vn.riverlee.lake_side_hotel.criteria;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String key;
    private String operator;
    private Object value;
}
