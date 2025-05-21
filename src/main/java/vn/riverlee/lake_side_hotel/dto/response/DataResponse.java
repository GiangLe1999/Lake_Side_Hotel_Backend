package vn.riverlee.lake_side_hotel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DataResponse<T> implements Serializable {
    private final int status;
    private final String message;
    // Khi chuyển đối tượng DataResponse thành JSON,
    // trường data sẽ chỉ được ghi vào JSON nếu nó khác null.
    // Nếu data == null thì trường này sẽ không xuất hiện trong JSON.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * Response data for the API to retrieve data successfully. For GET, POST only
     * @param status
     * @param message
     * @param data
     */
    public DataResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Response data when the API executes successfully or getting error. For PUT, PATCH, DELETE
     * @param status
     * @param message
     */
    public DataResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}