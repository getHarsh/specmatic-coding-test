package com.store.controllers

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import java.util.concurrent.ConcurrentHashMap
import java.time.LocalDateTime

@RestController
@RequestMapping("/products")
class ProductsController {
    private val products = ConcurrentHashMap<String, Product>()
    private var currentId = 1

    @PostMapping
    fun createProduct(@RequestBody product: ProductDetails): ResponseEntity<Any> {
        // Validate inventory
        if (product.inventory < 1 || product.inventory > 9999) {
            return ResponseEntity(createErrorResponse("Invalid inventory value", "/products"), HttpStatus.BAD_REQUEST)
        }

        // Validate product type
        val allowedTypes = listOf("book", "food", "gadget", "other")
        if (product.type !in allowedTypes) {
            return ResponseEntity(createErrorResponse("Invalid product type", "/products"), HttpStatus.BAD_REQUEST)
        }

        // Create new product
        val newProduct = Product(id = currentId++, name = product.name, type = product.type, inventory = product.inventory)
        products[newProduct.id.toString()] = newProduct

        val response = mapOf("id" to newProduct.id)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @GetMapping
    fun getProducts(@RequestParam(required = false) type: String?): ResponseEntity<Any> {
        if (type != null) {
            val allowedTypes = listOf("book", "food", "gadget", "other")
            if (type !in allowedTypes) {
                return ResponseEntity(createErrorResponse("Invalid product type", "/products"), HttpStatus.BAD_REQUEST)
            }
        }
        
        val filteredProducts = if (type != null) {
            products.values.filter { it.type == type }
        } else {
            products.values.toList()
        }
        return ResponseEntity(filteredProducts, HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: String): ResponseEntity<Any> {
        return if (products.containsKey(id)) {
            products.remove(id)
            ResponseEntity("Product deleted", HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(createErrorResponse("Product not found", "/products/$id"), HttpStatus.NOT_FOUND)
        }
    }

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: String, @RequestBody updatedProduct: ProductDetails): ResponseEntity<Any> {
        if (updatedProduct.inventory < 1 || updatedProduct.inventory > 9999) {
            return ResponseEntity(createErrorResponse("Invalid inventory value", "/products/$id"), HttpStatus.BAD_REQUEST)
        }

        val allowedTypes = listOf("book", "food", "gadget", "other")
        if (updatedProduct.type !in allowedTypes) {
            return ResponseEntity(createErrorResponse("Invalid product type", "/products/$id"), HttpStatus.BAD_REQUEST)
        }

        return if (products.containsKey(id)) {
            val product = products[id]!!.copy(name = updatedProduct.name, type = updatedProduct.type, inventory = updatedProduct.inventory)
            products[id] = product
            ResponseEntity("Product updated", HttpStatus.OK)
        } else {
            ResponseEntity(createErrorResponse("Product not found", "/products/$id"), HttpStatus.NOT_FOUND)
        }
    }

    private fun createErrorResponse(message: String, path: String): ErrorResponseBody {
        return ErrorResponseBody(
            timestamp = LocalDateTime.now().toString(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = message,
            path = path
        )
    }
}

data class ProductDetails(
    val name: String,
    val type: String,
    val inventory: Int
)

data class Product(
    val id: Int,
    val name: String,
    val type: String,
    val inventory: Int
)

data class ErrorResponseBody(
    val timestamp: String,
    val status: Int,
    val error: String,
    val path: String
)
