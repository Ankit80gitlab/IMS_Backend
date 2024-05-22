package com.cms.core.entity;

/**
 * Created by Shashidhar on 4/18/2024.
 */

import lombok.Getter; import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter @Setter
@Table(name = "comments_file")
public class CommentsFile implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comments_id", nullable = false)
    private Comment comment;

}

