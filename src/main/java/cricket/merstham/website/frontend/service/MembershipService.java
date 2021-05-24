package cricket.merstham.website.frontend.service;

import com.apollographql.apollo.api.Response;
import cricket.merstham.website.frontend.model.Order;
import cricket.merstham.website.frontend.model.RegistrationBasket;
import cricket.merstham.website.graph.CreateMemberMutation;
import cricket.merstham.website.graph.CreateOrderMutation;
import cricket.merstham.website.graph.type.AttributeInput;
import cricket.merstham.website.graph.type.MemberInput;
import cricket.merstham.website.graph.type.MemberSubscriptionInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MembershipService {
    private static final Logger LOG = LoggerFactory.getLogger(MembershipService.class);

    private GraphService graphService;

    @Autowired
    public MembershipService(GraphService graphService) {
        this.graphService = graphService;
    }

    public Order registerMembersFromBasket(RegistrationBasket basket, Principal principal) {
        CreateOrderMutation createOrder = new CreateOrderMutation(basket.getId());
        Order order = new Order();
        try {
            Response<CreateOrderMutation.Data> orderResult =
                    graphService.executeMutation(createOrder, principal);
            if (orderResult.hasErrors()) {
                throw new RuntimeException(
                        "GraphQL error(s) registering member: "
                                + String.join(
                                        "\n",
                                        orderResult.getErrors().stream()
                                                .map(error -> error.getMessage())
                                                .collect(Collectors.toList())));
            }
            order.setId(orderResult.getData().createOrder().id())
                    .setUuid(UUID.fromString(orderResult.getData().createOrder().uuid()))
                    .setTotal(basket.getBasketTotal());
        } catch (IOException e) {
            LOG.error("Error creating order", e);
            throw new RuntimeException(e);
        }

        for (var subscription : basket.getSubscriptions().entrySet()) {
            MemberInput memberInput =
                    MemberInput.builder()
                            .attributes(
                                    subscription.getValue().getMember().entrySet().stream()
                                            .map(
                                                    a ->
                                                            AttributeInput.builder()
                                                                    .key(a.getKey())
                                                                    .value(a.getValue())
                                                                    .build())
                                            .collect(Collectors.toList()))
                            .subscription(
                                    MemberSubscriptionInput.builder()
                                            .year(LocalDate.now().getYear())
                                            .pricelistItemId(
                                                    subscription.getValue().getPricelistItemId())
                                            .price(subscription.getValue().getPrice().doubleValue())
                                            .orderId(order.getId())
                                            .build())
                            .build();

            CreateMemberMutation createMemberMutation = new CreateMemberMutation(memberInput);
            try {
                var result = graphService.executeMutation(createMemberMutation, principal);
                if (result.hasErrors()) {
                    throw new RuntimeException(
                            "GraphQL error(s) registering member: "
                                    + String.join(
                                            "\n",
                                            result.getErrors().stream()
                                                    .map(error -> error.getMessage())
                                                    .collect(Collectors.toList())));
                }
            } catch (IOException e) {
                LOG.error("Error registering member", e);
                throw new RuntimeException(e);
            }
        }
        return order;
    }
}