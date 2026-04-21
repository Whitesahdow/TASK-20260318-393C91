1. Source of "Stop Popularity" Data
Question: Since the system is deployed in an offline LAN, what data source should be used to calculate "Stop Popularity" for the search ranking formula?
My Understanding: Popularity is usually dynamic behavior data. Without internet/external metrics, it must be generated within the platform.
Solution: Calculate popularity based on the frequency of passenger "Search Result Clicks" and "Arrival Reminder Activations" tracked within the platform.
2. Origin of "Reservations"
Question: The prompt requires notifications for "successful reservations," but does not mention a booking/reservation module. Is the reservation process handled inside this platform, or is this data imported from the HTML/JSON source?
My Understanding: The platform acts as a "Coordination Platform," suggesting it might be an aggregator.
Solution: Assume the reservation logic is external; the platform receives reservation records during the data integration/parsing phase and triggers notifications based on those imported records.
3. Workflow Branching Triggers
Question: The workflow engine supports "conditional branching," but the specific conditions for branching a "Route Data Change" are not defined.
My Understanding: Branching usually depends on the "Risk" or "Scope" of a change.
Solution: Implement branching based on field types: Changes to "Stop Names/Addresses" follow a standard single-dispatcher path, while changes to "GPS Coordinates" or "Prices" trigger the "Joint/Parallel Approval" branch.
4. Data Integration Input Method
Question: How are the "HTML/JSON templates" provided to the backend in an offline LAN environment?
My Understanding: Offline servers cannot "crawl" the web or call external APIs.
Solution: Implement a "Local Watcher" service. Administrators will manually place source files into a designated directory on the server; the Spring Boot service will detect, parse, and clean the data automatically upon file arrival.
5. Pinyin Matching Scope
Question: Should Pinyin/Initial letter matching apply to all searchable fields (Route Number, Stop Name, Keywords), or only to Chinese-character fields?
My Understanding: Pinyin is specifically for phonetic matching of Chinese characters, but initials (e.g., "M1" for "Metro 1") can apply to alphanumeric routes.
Solution: Apply Pinyin matching only to fields containing Chinese characters (Stop Name, Residential Area Name) and use "Prefix Matching" for alphanumeric Route Numbers.
6. 24-Hour Timeout Escalation Target
Question: When a task remains unprocessed for 24 hours, who specifically is the "Escalation Warning" sent to?
My Understanding: Escalation usually bypasses the current owner to a higher authority.
Solution: The system will send the Escalation Warning to all users with the "Operation Administrator" role and flag the task as "Overdue" on the Dispatcher's dashboard.

7. Sensitivity Levels for Desensitization
Question: The prompt requires content desensitization based on "sensitivity levels," but does not define the levels or the masks.
My Understanding: Different roles see different levels of detail in the Message Center.
Solution: I will implement 3 Levels:
Level 1 (Public): No masking.
Level 2 (Internal): Masking individual names (e.g., J**n Doe).
Level 3 (Private): Masking Apartment Prices and exact addresses.
Rules: Passengers see Level 3 (Highly Masked). Dispatchers see Level 2. Admins see Level 1 (Full Data).