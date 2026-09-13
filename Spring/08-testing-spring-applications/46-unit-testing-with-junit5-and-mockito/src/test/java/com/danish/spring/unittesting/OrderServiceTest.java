package com.danish.spring.unittesting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) - NOT @SpringBootTest, NOT @ExtendWith(SpringExtension.class).
// No ApplicationContext is created at all for this test class - no bean scanning, no
// auto-configuration, none of lesson 04's 52 mystery beans. This is what makes a true
// unit test fast: milliseconds, not the multi-second startup every earlier lesson's
// spring-boot:run went through.
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // @Mock creates a FAKE implementation of the interface - no real database, no real
    // pricing logic, just an object Mockito controls completely, on which every method
    // returns null/0/false until told otherwise with when(...).
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PricingService pricingService;

    private OrderService orderService;

    // Manual construction here (not @InjectMocks) - keeps the wiring visible and
    // explicit, exactly the constructor-injection style lesson 08 argued for.
    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, pricingService);
    }

    @Test
    @DisplayName("a valid order is priced, saved, and returned")
    void placesAValidOrder() {
        when(pricingService.unitPriceCents("desk-lamp")).thenReturn(2499);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(42L);
            return order;
        });

        Order result = orderService.placeOrder("desk-lamp", 1);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getTotalCents()).isEqualTo(2499);

        // verify() checks a mock was actually CALLED, with what arguments, how many
        // times - this is Mockito's other half beyond stubbing return values.
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("an ArgumentCaptor inspects exactly what was passed to save()")
    void capturesTheSavedOrder() {
        when(pricingService.unitPriceCents("keyboard")).thenReturn(4999);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder("keyboard", 2);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getProduct()).isEqualTo("keyboard");
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getTotalCents()).isEqualTo(9998);
    }

    @ParameterizedTest(name = "quantity {0} is rejected before pricing or saving ever run")
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("invalid quantities throw IllegalArgumentException")
    void rejectsInvalidQuantities(int badQuantity) {
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder("desk-lamp", badQuantity));

        // verifyNoInteractions proves the method failed FAST - neither dependency was
        // ever called, because the quantity check runs before either of them.
        verifyNoInteractions(pricingService, orderRepository);
    }

    @Test
    @DisplayName("an order below the minimum throws MinimumOrderException and is never saved")
    void rejectsOrdersBelowMinimum() {
        when(pricingService.unitPriceCents("sticker")).thenReturn(50); // 1 sticker = 50 cents, minimum is 500

        assertThrows(MinimumOrderException.class, () -> orderService.placeOrder("sticker", 1));

        verify(orderRepository, never()).save(any());
    }
}
