package vn.riverlee.lake_side_hotel.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchCriteriaQueryConsumer implements Consumer<SearchCriteria> {
    // CriteriaBuilder = bộ công cụ để bạn tạo truy vấn SQL bằng Java.
    // giúp bạn tạo ra các phần như: SELECT, WHERE, AND, OR, LIKE, >… tất cả bằng Java code
    private CriteriaBuilder builder;
    // Predicate là một đối tượng đại diện cho điều kiện WHERE trong truy vấn, được dùng để lọc dữ liệu trong Criteria API.
    // Predicate tích lũy kết quả của tất cả các điều kiện tìm kiếm, bạn có thể kết hợp nhiều điều kiện với nhau bằng and, or
    private Predicate predicate;
    // Root entity mà chúng ta đang truy vấn (Room)
    private Root<?> root;

    /**
     * Xử lý một tiêu chí tìm kiếm cụ thể và kết hợp nó vào predicate chung.
     *
     * @param param Tiêu chí tìm kiếm với key, operator và value
     */
    @Override
    // Khi gọi criteriaList.forEach(queryConsumer), mỗi phần tử sẽ được đưa vào hàm này.
    // forEach yêu cầu một đối tượng có thể xử lý từng phần tử của danh sách.
    // tức là bất kỳ đối tượng nào có hàm accept(T) (implement interface Consumer<T>) thì đều có thể truyền vào forEach
    // queryConsumer đã implement interface Consumer<SearchCriteria>, nên có sẵn phương thức accept(SearchCriteria).
    // Java sẽ gọi accept() với từng phần tử trong criteriaList để áp dụng logic tìm kiếm.
    // param là từng phần tử trong danh sách criteriaList (danh sách mà gọi forEach)
    public void accept(SearchCriteria param) {
        // Lấy tên trường từ SearchCriteria
        String key = param.getKey();
        // Lấy toán tử so sánh từ SearchCriteria
        String operator = param.getOperator();
        // Lấy giá trị cần so sánh từ SearchCriteria
        Object value = param.getValue();

        switch (operator) {
            case ">" ->
                // Thêm một điều kiện mới kiểu >= vào danh sách điều kiện đang có, và kết hợp tất cả điều kiện bằng AND.
                // Vì bạn đang duyệt qua nhiều tiêu chí trong danh sách
                // nên mỗi lần thêm điều kiện mới, bạn phải cộng dồn nó vào predicate bằng AND.
                // root.get(key) là cách Criteria API lấy ra một cột trong bảng dựa theo tên field (tên biến trong entity User)
                    predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get(key), value.toString()));
            case "<" -> predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(key), value.toString()));
            case ":" ->
                // Toán tử bằng hoặc chứa (cho chuỗi)
                    handleEqualSearch(key, value);
            case "~" ->
                // Xử lý tìm kiếm "chứa một trong các giá trị" cho collection fields
                    handleInSearch(key, value);
        }
    }

    /**
     * Xử lý tìm kiếm bằng (equal) hoặc chứa (like)
     */
    private void handleEqualSearch(String key, Object value) {
        if (root.get(key).getJavaType() == String.class) {
            // Nếu là chuỗi, sử dụng LIKE với wildcards (%)
            predicate = builder.and(predicate,
                    builder.like(builder.lower(root.get(key)),
                            "%" + value.toString().toLowerCase() + "%"));
        } else {
            predicate = builder.and(predicate, builder.equal(root.get(key), value.toString()));
        }
    }

    /**
     * Xử lý tìm kiếm "in" cho các trường collection và occupancy
     */
    private void handleInSearch(String key, Object value) {
        // Tách chuỗi giá trị thành danh sách (phân tách bằng dấu phẩy)
        List<String> values = Arrays.asList(value.toString().split("_"));

        if (key.equals("amenities") || key.equals("features")) {
            // Xử lý cho collection fields (amenities, features)
            handleCollectionInSearch(key, values);
        } else if (key.equals("occupancy")) {
            // Xử lý cho trường occupancy (số nguyên)
            handleOccupancyInSearch(values);
        } else {
            // Xử lý mặc định cho các trường khác
            handleDefaultInSearch(key, values);
        }
    }

    /**
     * Xử lý tìm kiếm "in" cho collection fields (amenities, features)
     */
    private void handleCollectionInSearch(String key, List<String> values) {
        Predicate[] predicates = new Predicate[values.size()];

        for (int i = 0; i < values.size(); i++) {
            String trimmedValue = values.get(i).trim();
            // Sử dụng isMember để kiểm tra xem giá trị có trong collection không
            predicates[i] = builder.isMember(trimmedValue, root.get(key));
        }

        // Kết hợp các điều kiện bằng OR (chỉ cần thỏa mãn một trong các giá trị)
        Predicate andPredicate = builder.and(predicates);
        predicate = builder.and(predicate, andPredicate);
    }

    /**
     * Xử lý tìm kiếm "in" cho trường occupancy
     */
    private void handleOccupancyInSearch(List<String> values) {
        Predicate[] predicates = new Predicate[values.size()];

        for (int i = 0; i < values.size(); i++) {
            try {
                Integer occupancyValue = Integer.parseInt(values.get(i).trim());
                predicates[i] = builder.equal(root.get("occupancy"), occupancyValue);
            } catch (NumberFormatException e) {
                // Bỏ qua các giá trị không phải số
                predicates[i] = builder.conjunction(); // Điều kiện luôn đúng
            }
        }

        // Kết hợp các điều kiện bằng OR
        Predicate orPredicate = builder.or(predicates);
        predicate = builder.and(predicate, orPredicate);
    }

    /**
     * Xử lý tìm kiếm "in" mặc định cho các trường khác
     */
    private void handleDefaultInSearch(String key, List<String> values) {
        Predicate[] predicates = new Predicate[values.size()];

        for (int i = 0; i < values.size(); i++) {
            String trimmedValue = values.get(i).trim();
            predicates[i] = builder.equal(root.get(key), trimmedValue);
        }

        // Kết hợp các điều kiện bằng OR
        Predicate orPredicate = builder.or(predicates);
        predicate = builder.and(predicate, orPredicate);
    }
}
