/*
    Implements endpoints related to customers.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Customer
import java.lang.Exception

typealias Customers = List<Customer>

class CustomerService(private val dal: AntaeusDal) {
    fun fetchAll(): Customers {
       return dal.fetchCustomers()
    }

    fun fetch(id: Int): Customer {
        return dal.fetchCustomer(id) ?: throw CustomerNotFoundException(id)
    }

    fun updateStatus(customer: Customer): Customer {
        return dal.updateCustomerStatus(customer) ?: throw Exception()
    }
}
