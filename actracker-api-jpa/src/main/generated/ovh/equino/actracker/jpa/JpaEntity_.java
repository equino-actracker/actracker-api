package ovh.equino.actracker.jpa;

import jakarta.persistence.metamodel.MappedSuperclassType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(JpaEntity.class)
public abstract class JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.JpaEntity#id
	 **/
	public static volatile SingularAttribute<JpaEntity, String> id;
	
	/**
	 * @see ovh.equino.actracker.jpa.JpaEntity
	 **/
	public static volatile MappedSuperclassType<JpaEntity> class_;

	public static final String ID = "id";

}

