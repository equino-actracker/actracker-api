package ovh.equino.actracker.jpa.notification;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(NotificationEntity.class)
public abstract class NotificationEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.notification.NotificationEntity#data
	 **/
	public static volatile SingularAttribute<NotificationEntity, String> data;
	
	/**
	 * @see ovh.equino.actracker.jpa.notification.NotificationEntity#dataType
	 **/
	public static volatile SingularAttribute<NotificationEntity, String> dataType;
	
	/**
	 * @see ovh.equino.actracker.jpa.notification.NotificationEntity
	 **/
	public static volatile EntityType<NotificationEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.notification.NotificationEntity#version
	 **/
	public static volatile SingularAttribute<NotificationEntity, Long> version;

	public static final String DATA = "data";
	public static final String DATA_TYPE = "dataType";
	public static final String VERSION = "version";

}

