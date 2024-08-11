package org.astu.attendancetracker.persistence.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.astu.attendancetracker.core.domain.TeacherProfile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TeacherRepositoryImpl {
    @PersistenceContext
    private EntityManager em;

    public void addTeacher(TeacherProfile teacherProfile) {
        em.persist(teacherProfile);
    }

    public List<TeacherProfile> getAllTeachers() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TeacherProfile> cq = cb.createQuery(TeacherProfile.class);
        Root<TeacherProfile> root = cq.from(TeacherProfile.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }
}
