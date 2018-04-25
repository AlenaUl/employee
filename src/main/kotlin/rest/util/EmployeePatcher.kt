package de.hska.employee.rest.util

import de.hska.employee.entity.Employee
import de.hska.employee.entity.SkillsType


/**
 * Singleton-Klasse, um PATCH-Operationen auf Employee-Objekte anzuwenden.
 */
object EmployeePatcher {
    /**
     * PATCH-Operationen werden auf ein Employee-Objekt angewandt.
     * @param employee Das zu modifizierende Employee-Objekt.
     * @param operations Die anzuwendenden Operationen.
     * @return Ein Employee-Objekt mit den modifizierten Properties.
     */
    fun patch(employee: Employee, operations: List<PatchOperation>): Employee {
        val replaceOps = operations.filter { "replace" == it.op }
        var employeeUpdated = replaceOps(employee, replaceOps)

        val addOps = operations.filter { "add" == it.op }
        employeeUpdated = addSkill(employeeUpdated, addOps)

        val removeOps = operations.filter { "remove" == it.op }
        return removeSkill(employeeUpdated, removeOps)
    }

    private
    fun replaceOps(employee: Employee, ops: Collection<PatchOperation>): Employee {
        var employeeUpdated = employee
        ops.forEach {
            when (it.path) {
                "/nachname" -> {
                    employeeUpdated = replaceLastname(employeeUpdated, it.value)
                }
                "/email" -> {
                    employeeUpdated = replaceEmail(employeeUpdated, it.value)
                }
            }
        }
        return employeeUpdated
    }

    private
    fun replaceLastname(employee: Employee, lastname: String) = employee.copy(lastname = lastname)

    private
    fun replaceEmail(employee: Employee, email: String) = employee.copy(email = email)

    private
    fun addSkill(employee: Employee, ops: Collection<PatchOperation>): Employee {
        if (ops.isEmpty()) {
            return employee
        }
        var employeeUpdated = employee
        ops.filter { "/skills" == it.path }
                .forEach { employeeUpdated = addSkill(it, employeeUpdated) }
        return employeeUpdated
    }

    private
    fun addSkill(op: PatchOperation, employee: Employee): Employee {
        val skillStr = op.value
        val skill = SkillsType.build(skillStr)
                ?: throw InvalidSkillException(skillStr)
        val skills = if (employee.skills == null)
            mutableListOf()
        else employee.skills.toMutableList()
        skills.add(skill)
        return employee.copy(skills = skills)
    }

    private
    fun removeSkill(employee: Employee, ops: List<PatchOperation>): Employee {
        if (ops.isEmpty()) {
            return employee
        }
        var employeeUpdated = employee
        ops.filter { "/skills" == it.path }
                .forEach { employeeUpdated = removeSkill(it, employee) }
        return employeeUpdated
    }

    private
    fun removeSkill(op: PatchOperation, employee: Employee): Employee {
        val skillStr = op.value
        val skill = SkillsType.build(skillStr)
                ?: throw InvalidSkillException(skillStr)
        val skills = employee.skills?.filter { it != skill }
        return employee.copy(skills = skills)
    }
}

class InvalidSkillException(value: String)
    : RuntimeException("$value ist nicht gültig")
