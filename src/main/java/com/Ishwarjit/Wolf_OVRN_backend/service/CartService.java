package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.CartItemRequest;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CartResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateCartItemRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Cart;
import com.Ishwarjit.Wolf_OVRN_backend.entity.CartItem;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Product;
import com.Ishwarjit.Wolf_OVRN_backend.entity.User;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.CartItemRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.CartRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.ProductRepository;
import com.Ishwarjit.Wolf_OVRN_backend.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public CartResponse getCart(String userId) {
        UUID uuid = UUID.fromString(userId);
        Cart cart = getOrCreateCart(uuid);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse addItem(String userId, CartItemRequest request) {
        UUID uuid = UUID.fromString(userId);
        Cart cart = getOrCreateCart(uuid);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + request.getProductId()));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ResourceNotFoundException("Product not available");
        }

        cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setQuantity(existing.getQuantity() + request.getQuantity());
                            cartItemRepository.save(existing);
                        },
                        () -> {
                            CartItem fresh = new CartItem();
                            fresh.setCart(cart);
                            fresh.setProduct(product);
                            fresh.setQuantity(request.getQuantity());
                            CartItem saved = cartItemRepository.save(fresh);
                            cart.getItems().add(saved);
                        });

        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse updateItem(String userId, UUID productId, UpdateCartItemRequest request) {
        UUID uuid = UUID.fromString(userId);
        Cart cart = getOrCreateCart(uuid);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found for product: " + productId));

        if (request.getQuantity() == 0) {
            cartItemRepository.delete(item);
            cart.getItems().remove(item);
        } else {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeItem(String userId, UUID productId) {
        UUID uuid = UUID.fromString(userId);
        Cart cart = getOrCreateCart(uuid);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found for product: " + productId));

        cartItemRepository.delete(item);
        cart.getItems().remove(item);

        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse clearCart(String userId) {
        UUID uuid = UUID.fromString(userId);
        Cart cart = getOrCreateCart(uuid);

        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();

        return CartResponse.from(cart);
    }
}
