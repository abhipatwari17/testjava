package com.example.thoughtclan.conversion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractBaseEntity implements Serializable {

    private static final long serialVersionUID = -6344339181584600788L;

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column( columnDefinition = "BINARY(16)" )
    private UUID id;

    @Column( name = "created", nullable = false, updatable = false )
    @CreatedDate
    private LocalDateTime created;

    @Column( name = "modified" )
    @LastModifiedDate
    private LocalDateTime modified;

    @Column( name = "created_by", columnDefinition = "BINARY(16)", updatable = false )
    @CreatedBy
    private String createdBy;

    @Column( name = "modified_by", columnDefinition = "BINARY(16)" )
    @LastModifiedBy
    private String modifiedBy;

    /**
     * Returns hash code of the class name. While hashcode and equals should have the same logic, in this case, it won't
     * work as "id" will be generated only when the entity is being persisted for the first time.
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    /**
     * Returns equality based upon id of the object.
     */
    @Override
    public boolean equals( final Object obj ) {
        if( this == obj ) {
            return true;
        }
        if( obj == null ) {
            return false;
        }
        if( !getClass().equals( obj.getClass() ) ) {
            return false;
        }
        final AbstractBaseEntity other = (AbstractBaseEntity) obj;
        return getId() != null && getId().equals( other.getId() );
    }
}
