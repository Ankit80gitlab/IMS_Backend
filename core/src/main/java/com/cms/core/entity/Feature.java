package com.cms.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name="feature")
public class Feature implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String path;

	@OneToMany(mappedBy = "feature", fetch = FetchType.LAZY)
	private Set<RoleFeatureMapping> roleFeatureMappings;
}
