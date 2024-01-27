package ovh.equino.actracker.jpa.tag;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(MetricEntity.class)
public abstract class MetricEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.tag.MetricEntity#deleted
	 **/
	public static volatile SingularAttribute<MetricEntity, Boolean> deleted;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.MetricEntity#creatorId
	 **/
	public static volatile SingularAttribute<MetricEntity, String> creatorId;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.MetricEntity#name
	 **/
	public static volatile SingularAttribute<MetricEntity, String> name;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.MetricEntity#tag
	 **/
	public static volatile SingularAttribute<MetricEntity, TagEntity> tag;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.MetricEntity#type
	 **/
	public static volatile SingularAttribute<MetricEntity, String> type;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.MetricEntity
	 **/
	public static volatile EntityType<MetricEntity> class_;

	public static final String DELETED = "deleted";
	public static final String CREATOR_ID = "creatorId";
	public static final String NAME = "name";
	public static final String TAG = "tag";
	public static final String TYPE = "type";

}

