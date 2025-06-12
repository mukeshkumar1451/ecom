package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.modal.Cart;
import com.ecommerce.project.modal.CartItem;
import com.ecommerce.project.modal.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart  = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });
        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No carts found");
        }
        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                        ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                        productDTO.setQuantity(cartItem.getQuantity());
                        return productDTO;
                            }).toList();

                            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();

        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems()
                .forEach(c -> c.getProduct()
                        .setQuantity(c.getQuantity()));

        List<ProductDTO> productDTOS = cart.getCartItems().stream().map(
                p -> modelMapper.map(p.getProduct(), ProductDTO.class)
        ).toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {


        String emailId = authUtil.loggedInEmail();
        Cart usercart = cartRepository.findCartByEmail(emailId);
        Long cartId = usercart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0){
            throw new APIException(product.getProductName()+" is not available");
        }
        if(product.getQuantity()<quantity){
            throw new APIException("please, make an order of the "+product.getProductName()
                    +" less than or equal to the quantity "+product.getQuantity()+".");
        }
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not found in the cart");
        }
        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0){
            throw new APIException("Product quantity cannot be negative");
        }
        if(newQuantity == 0){
            deleteProductFromCart(cartId, productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartItemRepository.save(cartItem);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);
        if (updatedItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            throw  new ResourceNotFoundException("Product", "productId", productId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        Product product =cartItem.getProduct();
        //product.setQuantity(product.getQuantity() + cartItem.getQuantity());
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from cart!!";
    }

    private Cart createCart() {
        Cart userCart  = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart =  cartRepository.save(cart);

        return newCart;
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId){
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem =cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {

    String emailId = authUtil.loggedInEmail();
    Cart existingCart = cartRepository.findCartByEmail(emailId);
    if (existingCart == null) {
        Cart newCart = new Cart();
        newCart.setUser(authUtil.loggedInUser());
        newCart.setTotalPrice(0.0);
        existingCart = cartRepository.save(newCart);
    } else {
        cartItemRepository.deleteAllByCartId(existingCart.getCartId());
    }
    double totalPrice = 0.0;
    for (CartItemDTO itemDTO : cartItems) {
        Long productId = itemDTO.getProductId();
        Integer quantity = itemDTO.getQuantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

       // product.setQuantity(product.getQuantity() - quantity);
        totalPrice += product.getSpecialPrice() * quantity;

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setCart(existingCart);
        cartItem.setQuantity(quantity);
        cartItem.setDiscount(product.getDiscount());
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItemRepository.save(cartItem);
    }
    existingCart.setTotalPrice(totalPrice);
    cartRepository.save(existingCart);
    return "Cart created/updated successfully with new items!";

    }
}
