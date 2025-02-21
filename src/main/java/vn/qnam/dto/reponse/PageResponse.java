package vn.qnam.dto.reponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
public class PageResponse<T> implements Serializable {
    private int pageNo;
    private int pageSize;
    private int totalPage;
    private T items;
}
