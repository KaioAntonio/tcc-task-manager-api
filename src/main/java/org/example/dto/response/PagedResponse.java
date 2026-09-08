package org.example.dto.response;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
/**
 * Resposta paginada padronizada para todos os endpoints de listagem.
 */
@Getter @Builder
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
