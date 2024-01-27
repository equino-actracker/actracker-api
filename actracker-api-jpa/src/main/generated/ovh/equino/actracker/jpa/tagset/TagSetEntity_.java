package ovh.equino.actracker.jpa.tagset;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import ovh.equino.actracker.jpa.tag.TagEntity;

@StaticMetamodel(TagSetEntity.class)
public abstract class TagSetEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.tagset.TagSetEntity#deleted
	 **/
	public static volatile SingularAttribute<TagSetEntity, Boolean> deleted;
	
	/**
	 * @see ovh.equino.actracker.jpa.tagset.TagSetEntity#creatorId
	 **/
	public static volatile SingularAttribute<TagSetEntity, String> creatorId;
	
	/**
	 * @see ovh.equino.actracker.jpa.tagset.TagSetEntity#name
	 **/
	public static volatile SingularAttribute<TagSetEntity, String> name;
	
	/**
	 * @see ovh.equino.actracker.jpa.tagset.TagSetEntity
	 **/
	public static volatile EntityType<TagSetEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.tagset.TagSetEntity#tags
	 **/
	public static volatile SetAttribute<TagSetEntity, TagEntity> tags;

	public static final String DELETED = "deleted";
	public static final String CREATOR_ID = "creatorId";
	public static final String NAME = "name";
	public static final String TAGS = "tags";

}

