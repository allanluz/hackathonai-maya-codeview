package com.sinqia.maya.repository.impl;

import com.sinqia.maya.entity.CodeReview;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação customizada para filtros complexos de CodeReview.
 * 
 * @author Sistema MAYA
 * @version 1.0.0
 */
@Repository
public class CodeReviewRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<CodeReview> findWithFilters(String repositoryName, String projectName, String author, 
                                          CodeReview.ReviewStatus status, Pageable pageable) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CodeReview> query = cb.createQuery(CodeReview.class);
        Root<CodeReview> root = query.from(CodeReview.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Aplicar filtros condicionalmente
        if (repositoryName != null && !repositoryName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("repositoryName")), 
                                 "%" + repositoryName.toLowerCase() + "%"));
        }
        
        if (projectName != null && !projectName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("projectName")), 
                                 "%" + projectName.toLowerCase() + "%"));
        }
        
        if (author != null && !author.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("author")), 
                                 "%" + author.toLowerCase() + "%"));
        }
        
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        
        // Combinar predicados
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // Ordenação
        query.orderBy(cb.desc(root.get("createdAt")));
        
        // Criar query
        TypedQuery<CodeReview> typedQuery = entityManager.createQuery(query);
        
        // Paginação
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<CodeReview> results = typedQuery.getResultList();
        
        // Contar total para paginação
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CodeReview> countRoot = countQuery.from(CodeReview.class);
        countQuery.select(cb.count(countRoot));
        
        // Aplicar os mesmos filtros na query de contagem
        List<Predicate> countPredicates = new ArrayList<>();
        
        if (repositoryName != null && !repositoryName.trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("repositoryName")), 
                                      "%" + repositoryName.toLowerCase() + "%"));
        }
        
        if (projectName != null && !projectName.trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("projectName")), 
                                      "%" + projectName.toLowerCase() + "%"));
        }
        
        if (author != null && !author.trim().isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countRoot.get("author")), 
                                      "%" + author.toLowerCase() + "%"));
        }
        
        if (status != null) {
            countPredicates.add(cb.equal(countRoot.get("status"), status));
        }
        
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }
}
