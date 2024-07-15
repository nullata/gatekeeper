package com.gatekeeper.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author null
 */
@Entity
@Table(name = "api_tokens", catalog = "gatekeeper", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_tokens"})})
@NamedQueries({
    @NamedQuery(name = "ApiTokens.findAll", query = "SELECT a FROM ApiTokens a"),
    @NamedQuery(name = "ApiTokens.findById", query = "SELECT a FROM ApiTokens a WHERE a.id = :id"),
    @NamedQuery(name = "ApiTokens.findByUserTokens", query = "SELECT a FROM ApiTokens a WHERE a.userTokens = :userTokens")})
public class ApiTokens implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic(optional = false)
    @Column(name = "user_tokens", nullable = false, length = 50)
    private String userTokens;

    public ApiTokens() {
    }

    public ApiTokens(Long id) {
        this.id = id;
    }

    public ApiTokens(Long id, String userTokens) {
        this.id = id;
        this.userTokens = userTokens;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserTokens() {
        return userTokens;
    }

    public void setUserTokens(String userTokens) {
        this.userTokens = userTokens;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ApiTokens)) {
            return false;
        }
        ApiTokens other = (ApiTokens) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.gatekeeper.entity.ApiTokens[ id=" + id + " ]";
    }
    
}
