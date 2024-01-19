package ovh.equino.actracker.jpa.activity;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.sql.Timestamp;
import ovh.equino.actracker.jpa.tag.TagEntity;

@StaticMetamodel(ActivityEntity.class)
public abstract class ActivityEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#deleted
	 **/
	public static volatile SingularAttribute<ActivityEntity, Boolean> deleted;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#creatorId
	 **/
	public static volatile SingularAttribute<ActivityEntity, String> creatorId;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#startTime
	 **/
	public static volatile SingularAttribute<ActivityEntity, Timestamp> startTime;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#comment
	 **/
	public static volatile SingularAttribute<ActivityEntity, String> comment;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#endTime
	 **/
	public static volatile SingularAttribute<ActivityEntity, Timestamp> endTime;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#title
	 **/
	public static volatile SingularAttribute<ActivityEntity, String> title;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity
	 **/
	public static volatile EntityType<ActivityEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#tags
	 **/
	public static volatile SetAttribute<ActivityEntity, TagEntity> tags;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.ActivityEntity#metricValues
	 **/
	public static volatile ListAttribute<ActivityEntity, MetricValueEntity> metricValues;

	public static final String DELETED = "deleted";
	public static final String CREATOR_ID = "creatorId";
	public static final String START_TIME = "startTime";
	public static final String COMMENT = "comment";
	public static final String END_TIME = "endTime";
	public static final String TITLE = "title";
	public static final String TAGS = "tags";
	public static final String METRIC_VALUES = "metricValues";

}

