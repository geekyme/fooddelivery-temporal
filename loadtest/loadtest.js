import { sleep } from "k6";
import http from "k6/http";
import uuid from "./uuid.js";

// The following config would have k6 ramping up from 1 to 10 VUs for 3 minutes,
// then staying flat at 10 VUs for 5 minutes, then ramping up from 10 to 35 VUs
// over the next 10 minutes before finally ramping down to 0 VUs for another
// 3 minutes.

export let options = {
  stages: [
    { duration: "2m", target: 100 },
    { duration: "5m", target: 500 },
    { duration: "3m", target: 0 },
  ],
};

export default function () {
  let id = uuid.v4();

  // create new customer order
  http.post(
    "http://localhost:8082/graphql",
    JSON.stringify({
      query: `
    mutation {
      fulfillOrder(order: { id: "${id}" }, isLoadtest:true) {
        id
      }
    }
  `,
    })
  );
}
