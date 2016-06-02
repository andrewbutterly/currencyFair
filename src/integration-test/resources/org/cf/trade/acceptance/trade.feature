Feature: Trade Requests

  Scenario: request create trade, trade created
    Given Trade Request Sent - sell: "10.00" rate: "2.00", currency from: "EUR", currency to: "USD", country: "IE"
    Then The service holds at least that trade in value - sell: "10.00" rate: "2.00", currency from: "EUR", currency to: "USD", country: "IE"
   