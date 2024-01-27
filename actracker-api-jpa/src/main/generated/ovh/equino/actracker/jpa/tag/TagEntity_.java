package ovh.equino.actracker.jpa.tag;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TagEntity.class)
public abstract class TagEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagEntity#shares
	 **/
	public static volatile ListAttribute<TagEntity, TagShareEntity> shares;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagEntity#deleted
	 **/
	public static volatile SingularAttribute<TagEntity, Boolean> deleted;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagEntity#creatorId
	 **/
	public static volatile SingularAttribute<TagEntity, String> creatorId;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagEntity#name
	 **/
	public static volatile SingularAttribute<TagEntity, String> name;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagEntity#metrics
	 **/
	public static volatile ListAttribute<TagEntity, MetricEntity> metrics;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagEntity
	 **/
	public static volatile EntityType<TagEntity> class_;

	public static final String SHARES = "shares";
	public static final String DELETED = "deleted";
	public static final String CREATOR_ID = "creatorId";
	public static final String NAME = "name";
	public static final String METRICS = "metrics";

}

