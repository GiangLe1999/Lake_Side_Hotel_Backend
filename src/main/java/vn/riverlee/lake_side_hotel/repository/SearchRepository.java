package vn.riverlee.lake_side_hotel.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.PageRequest;
import vn.riverlee.lake_side_hotel.criteria.RoomSearchCriteriaQueryConsumer;
import vn.riverlee.lake_side_hotel.criteria.SearchCriteria;
import vn.riverlee.lake_side_hotel.dto.response.ChatConversationResponse;
import vn.riverlee.lake_side_hotel.dto.response.PaginationResponse;
import vn.riverlee.lake_side_hotel.dto.response.RoomResponse;
import vn.riverlee.lake_side_hotel.enums.ChatStatus;
import vn.riverlee.lake_side_hotel.model.ChatConversation;
import vn.riverlee.lake_side_hotel.model.Room;
import vn.riverlee.lake_side_hotel.util.ChatMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;
    private final ChatMapper chatMapper;

    /**
     * Lấy danh sách phòng thỏa mãn các tiêu chí tìm kiếm
     *
     * @param pageNo Số trang
     * @param pageSize Kích thước trang
     * @param criteriaList Danh sách các tiêu chí tìm kiếm
     * @param sortBy Tiêu chí sắp xếp
     * @return Danh sách người dùng
     */
    private List<Room> getRooms(int pageNo, int pageSize, List<SearchCriteria> criteriaList, String sortBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // query là một đối tượng của CriteriaQuery<Room>, dùng trong Criteria API của JPA để xây dựng
        // và thực thi một truy vấn đối tượng (object-oriented query) cho entity Room.
        CriteriaQuery<Room> query = criteriaBuilder.createQuery(Room.class);
        // root là một đối tượng mà bạn sẽ sử dụng để truy xuất các trường (cột) trong bảng Room.
        // root.get("name") sẽ lấy giá trị của trường name trong bảng Room.
        Root<Room> root = query.from(Room.class);

        // Đoạn mã này tạo ra một predicate mặc định, tương đương với điều kiện true (hoặc "không có điều kiện gì cả")
        // Khi bạn sử dụng conjunction(), nó giống như bạn bắt đầu với một điều kiện "đúng" và sau đó nối thêm các điều kiện khác vào.
        // Nó được sử dụng khi bạn muốn nối các điều kiện bằng AND.
        // Ta có thể sử dụng disjunction() khi bạn muốn các điều kiện "hoặc" (OR):
        // disjunction() tạo ra một Predicate tương đương với true (nhưng để nối các điều kiện bằng OR thay vì AND).
        //Nếu bạn muốn điều kiện của mình là sự lựa chọn giữa các điều kiện, thì bạn sẽ sử dụng disjunction() thay vì conjunction().
        Predicate predicate = criteriaBuilder.conjunction();
        RoomSearchCriteriaQueryConsumer queryConsumer = new RoomSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        // Mỗi phần tử trong danh sách này criteriaList (ví dụ SearchCriteria("age", ">", 18))
        // sẽ lần lượt được truyền vào phương thức accept của queryConsumer
        // Tức là forEach sẽ lặp qua từng phần tử của criteriaList và gọi queryConsumer.accept(param) cho mỗi phần tử.
        // Mỗi lần gọi accept(), phương thức accept sẽ thêm một điều kiện vào predicate.
        // Kết quả cuối cùng là predicate sẽ đại diện cho toàn bộ các điều kiện WHERE trong truy vấn SQL.
        criteriaList.forEach(queryConsumer);

        // Lấy ra predicate cuối cùng chứa toàn bộ điều kiện WHERE hiện đang được lưu trong đối tượng queryConsumer
        predicate = queryConsumer.getPredicate();

        // Thêm điều kiện lọc vào trong truy vấn Criteria.
        // Sau khi gọi query.where(predicate), truy vấn sẽ chỉ trả về các bản ghi thỏa mãn điều kiện mà bạn đã xác định trong predicate.
        query.where(predicate);

        // Thêm sắp xếp nếu có
        if (sortBy != null && !sortBy.isEmpty()) {
            addSorting(query, criteriaBuilder, root, sortBy);
        }

        // Thực thi truy vấn được xây dựng từ Criteria API và lấy dữ liệu từ cơ sở dữ liệu.
        // Cụ thể, nó sử dụng entityManager để tạo và thực thi một truy vấn, với các tham số phân trang (pageNo và pageSize) và trả về kết quả dưới dạng danh sách đối tượng (List).
        return entityManager.createQuery(query).setFirstResult(pageNo).setMaxResults(pageSize).getResultList();
    }

    /**
     * Thêm logic sắp xếp
     */
    private void addSorting(CriteriaQuery<Room> query, CriteriaBuilder criteriaBuilder, Root<Room> root, String sortBy) {
        if (sortBy.startsWith("-")) {
            // Sắp xếp giảm dần
            String field = sortBy.substring(1);
            query.orderBy(criteriaBuilder.desc(root.get(field)));
        } else {
            // Sắp xếp tăng dần
            query.orderBy(criteriaBuilder.asc(root.get(sortBy)));
        }
    }

    /**
     * Đếm tổng số phòng dùng thỏa mãn các tiêu chí tìm kiếm
     *
     * @param criteriaList Danh sách các tiêu chí tìm kiếm
     * @return Tổng số phòng thỏa mãn
     */
    private long countRooms(List<SearchCriteria> criteriaList) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Room> root = countQuery.from(Room.class);

        Predicate predicate = criteriaBuilder.conjunction();
        RoomSearchCriteriaQueryConsumer queryConsumer = new RoomSearchCriteriaQueryConsumer(criteriaBuilder, predicate, root);

        criteriaList.forEach(queryConsumer);
        predicate = queryConsumer.getPredicate();

        // Thiết lập câu truy vấn đếm
        countQuery.select(criteriaBuilder.count(root)).where(predicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    public PaginationResponse<?> advanceSearchForRooms(int pageNo, int pageSize, String sortBy, String... search) {
        // Khởi tạo danh sách các tiêu chí tìm kiếm
        List<SearchCriteria> criteriaList = new ArrayList<>();
        if (search != null) {
            for (String s : search) {
                // Regex để phân tích các tiêu chí tìm kiếm
                // Nhóm 1: tên trường (fieldName) - \w+ một hoặc nhiều ký tự chữ-số
                // Nhóm 2: toán tử (:|>|<|in)
                // Nhóm 3: giá trị tìm kiếm (.*)
                Pattern pattern = Pattern.compile("(\\w+?)(:|>|<|~)(.*)");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    String field = matcher.group(1);
                    String operator = matcher.group(2);
                    String value = matcher.group(3);

                    System.out.println("field:" + field);
                    System.out.println("operator:" + operator);
                    System.out.println("value:" + value);

                    criteriaList.add(new SearchCriteria(field, operator, value));
                }
            }
        }

        // Lấy danh sách phòng dùng theo các tiêu chí
        List<Room> rooms = getRooms(pageNo, pageSize, criteriaList, sortBy);
        List<RoomResponse> roomsResponse = rooms.stream().map(room -> RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .type(room.getType())
                .summary(room.getSummary())
                .description(room.getDescription())
                .area(room.getArea())
                .beds(room.getBeds())
                .amenities(room.getAmenities())
                .features(room.getFeatures())
                .thumbnailKey(room.getThumbnailKey())
                .price(room.getPrice())
                .reviewCount(room.getReviewCount())
                .avgRating(room.getAvgRating())
                .createdAt(room.getCreatedAt())
                .build()).toList();

        // Đếm tổng số bản ghi thỏa mãn các tiêu chí để tính số trang
        long totalItems = countRooms(criteriaList);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // Trả về kết quả phân trang
        return PaginationResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(roomsResponse)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .build();
    }


    public PaginationResponse<?> getConversations(int pageNo, int pageSize, String search, String sortBy, String status) {
        StringBuilder sqlQuery = new StringBuilder(
                "select distinct c from ChatConversation c " +
                        "left join c.messages m " +
                        "left join c.user u " +   // Join thêm bảng User
                        "where 1=1"
        );

        if (StringUtils.hasLength(search)) {
            sqlQuery.append(" and (lower(c.guestName) like :search " +
                    "or lower(c.guestEmail) like :search " +
                    "or lower(m.content) like :search " +
                    "or lower(u.fullName) like :search " +    // Search theo user fullName
                    "or lower(u.email) like :search)");       // Search theo user email
        }

        if (status != null && !status.equals("ALL")) {
            sqlQuery.append(" and c.status = :status");
        }

        // Validate sortBy để tránh SQL Injection
        String sortDirection = "desc"; // default
        if ("asc".equalsIgnoreCase(sortBy)) {
            sortDirection = "asc";
        } else if ("desc".equalsIgnoreCase(sortBy)) {
            sortDirection = "desc";
        }

        sqlQuery.append(" order by c.lastMessageAt ").append(sortDirection);

        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(pageNo * pageSize);
        selectQuery.setMaxResults(pageSize);

        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        if (status != null && !status.equals("ALL")) {
            selectQuery.setParameter("status", ChatStatus.valueOf(status));
        }

        List<ChatConversation> resultList = selectQuery.getResultList();
        List<ChatConversationResponse> conversations = resultList.stream()
                .map(chatMapper::toDTOWithoutMessages)
                .toList();

        // Count Query (không cần order by)
        StringBuilder sqlCountQuery = new StringBuilder(
                "select count(distinct c) from ChatConversation c " +
                        "left join c.messages m " +
                        "left join c.user u " +   // Join thêm bảng User
                        "where 1=1"
        );

        if (StringUtils.hasLength(search)) {
            sqlCountQuery.append(" and (lower(c.guestName) like :search " +
                    "or lower(c.guestEmail) like :search " +
                    "or lower(m.content) like :search " +
                    "or lower(u.fullName) like :search " +
                    "or lower(u.email) like :search)");
        }

        if (status != null && !status.equals("ALL")) {
            sqlCountQuery.append(" and c.status = :status");
        }

        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());

        if (StringUtils.hasLength(search)) {
            selectCountQuery.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        if (status != null && !status.equals("ALL")) {
            selectCountQuery.setParameter("status", ChatStatus.valueOf(status));
        }

        Long totalItems = (Long) selectCountQuery.getSingleResult();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<ChatConversationResponse> page = new PageImpl<>(conversations, pageable, totalItems);

        boolean hasNextPage = (pageNo + 1) * pageSize < totalItems;

        return PaginationResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(conversations)
                .totalPages(page.getTotalPages())
                .hasNextPage(hasNextPage)
                .build();
    }
}
