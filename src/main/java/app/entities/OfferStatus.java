package app.entities;

public enum OfferStatus {
    // A customer has generated an offer
    // now a salesperson needs to review it
    SALESPERSON,

    // A salesperson has made an offer
    // now customer needs to accept/reject
    CUSTOMER,

    // The customer has accepted the offer
    // by paying. Now they can get a PDF
    ORDERED,

    // For the customer to accept an incoming offer
    ACCEPTED,

    // For the customer to reject an incoming offer
    REJECTED
}
