//package com.example.java_template.common.persistence;
//import com.example.java_template.common.exception.ResourceNotFoundException;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//public interface BaseRepository<T,ID> extends JpaRepository<T,ID> {
//    default T findByIdOrThrow(ID id){
//        return findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
//    }
//}
