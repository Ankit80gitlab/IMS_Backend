package com.cms.core.repository;

import com.cms.core.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Shashidhar on 6/3/2024.
 */
public interface CommentRepository extends JpaRepository<Comment,Integer> {
}
