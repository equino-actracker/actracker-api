package ovh.equino.actracker.jpa.tag;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TagShareEntity.class)
public abstract class TagShareEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagShareEntity#granteeId
	 **/
	public static volatile SingularAttribute<TagShareEntity, String> granteeId;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagShareEntity#tag
	 **/
	public static volatile SingularAttribute<TagShareEntity, TagEntity> tag;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagShareEntity
	 **/
	public static volatile EntityType<TagShareEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.tag.TagShareEntity#granteeName
	 **/
	public static volatile SingularAttribute<TagShareEntity, String> granteeName;

	public static final String GRANTEE_ID = "granteeId";
	public static final String TAG = "tag";
	public static final String GRANTEE_NAME = "granteeName";

}

