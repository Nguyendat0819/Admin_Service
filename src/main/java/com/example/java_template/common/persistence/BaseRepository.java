package com.example.java_template.common.persistence;

import com.example.java_template.common.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository cung cấp các helper method dùng chung cho mọi entity.
 *
 * <p>Annotation {@code @NoRepositoryBean} để Spring KHÔNG tự tạo implementation
 * cho interface generic này, chỉ dùng làm parent cho các repository cụ thể.</p>
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Tìm entity theo ID, ném {@link ResourceNotFoundException} nếu không tồn tại.
     */
    default T findByIdOrThrow(ID id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    /**
     * Kiểm tra entity có tồn tại theo ID hay không.
     */
    default boolean existsByIdOrThrow(ID id) {
        if (!existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        return true;
    }
}
