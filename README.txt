Alexander Schuler. CSE241, spring 2017

This was a final project for a Databases course. The idea was to create a
relational design for a rental car company and then implement a basic command
line interface for it in java using JDBC. All SQL creation code is in the sql
folder, while java is inside of src.

This program is broken up into a customer interface and a managerial interface.
The customer interface is designed in more of a manner as to be used by an
employee helping a customer rent or reserve vehicles, but also includes ways for
a customer to view their past rental history, currently active rentals, and
future reservations.

key features:
    storage note:
    reservations and rentals are treated the same way and are all stored in the
    rental table. A rental can be in one of 3 states, as indicated by 2 flags,
    pickup_occ and dropoff_occ, standing for whether or not pickup and dropoff
    occurred. A rental which has not been picked up will be shown as such by
    setting pickup_occ to 0. A rental in progress will have the pickup_occ flag
    set to 1, and dropoff_occ set to 0, to indicate that it has been picked up
    and dropped off. At this point, it is also not in any relation pertaining to
    location, as the location won't be known while a car is out. When a car is
    returned, the dropoff_occ flag is set to 1, and the car is placed into a
    relation pertaining to the dropoff location. 

    error handling:
    Should an error occur at any point within an "activity", the user will
    likely be booted from it or asked to restart. Data entered is saved however,
    and returning to the activity after failure will then prompt the user
    whether or not they want to keep the same values or use different ones. Some
    cases which may force the user to start over: creating a new account where
    the state associated with an address or license is not a valid US state.
    (e.g. UA). This fails at a database level due to a check constraint, and
    then user will then be asked to re-enter things. Another example is if a
    rental ends with more fuel than it started with ("hurts rent-a-car always
    starts rentals with a full tank"). you can see this for yourself by trying
    to create a new customer and not inputting a valid state abbreviation.

    time-changing:
    since rental car companies exist over a stretch of time and operate
    throughout time, I decided to implement a time system which will allow the
    grader to advance through time using the "set date" option. this allows the
    user to advance the date and optionally advance all rentals assuming that
    they were picked up and dropped off on time.  The date is currently
    somewhere around October of 2001, and rentals currently extend through the
    end of 2001. The current date can be viewed from either interface with the
    "get date" option. Most other functions of the database are time-sensitive,
    e.g. pickup of a car can only be done on or after the reserved start date,
    but not after the end date for a rental.

    making reservations:
    Car renting takes place in 2 steps: first, a reservation must be made,
    regardless of whether or not the customer is picking up the car the same
    day. This reservation takes into account a number of factors to determine
    which cars will be available. In order for a car to be available for rental,
    it must not already be previously reserved during the desired time frame,
    the last rental before the proposed rental must have a dropoff
    location matching the proposed rental's pickup location. (Hurts rent-a-car
    will absolutely not be wasting any time or money shipping cars across the
    country so people can rent them) Additionally, if a rental reservation
    exists after the proposed rental, the proposed rental must drop off the car
    at the pickup location of the next rental, because, again, we will not be
    moving around any cars in our enterprise. (To see the SQL logic for this,
    take a look at Queries.java in the source folder, and see the string
    AVAILABLE_CARS.

    picking up cars:
    In order to rent a car out of the database, the current time (in the
    my_time) relation must be on or after the reservation's start date. (we
    allow late pickups but early ones aren't cool) Additionally, the car must be
    picked up within the timeframe of the rental. (so people can't pick up after
    the rental period is over). However, there could still exist complications
    with this, say if a prior renter doesn't return the car when someone wants
    to pick it up. In this case, the user is notified that their reservation has
    been removed as they go to pick up the car, and are asked to create a new
    reservation. At pickup time, the interface asks about what the daily
    insurance charge on the car will be, and for miscelaneous charges to add to
    the rental. This time also allows the customer to enter an organization and
    associated promotional codes in order to discount the cost of their rental.

    dropping off cars:
    When a car is dropped off, the end date of the rental is set to the max of
    the current date and the original dropoff date. This means that a customer
    is still charged if they take the car out for more than their originally
    planned rental time. At dropoff, all charges and the cost of the rental is
    computed, and the car is "returned" in the database into the table storing
    locations for every car.

    creating new accounts:
    When the customer interface is first selected, the user can create a new
    account. This process asks for a license, address, and various other fields
    about the customer, and, upon successful completion, logs the customer in.

    logging in:
    when the customer interface is first selected, you are asked to sign in
    using and ID and a last name. These can be looked up in the customer
    relation, or you can use id:129 and last name: "allen", which I used to test
    a few things with car rentals. Additionally, entering "test" will give you a
    random ID to play around with.

    adding organizations to a user:
    a user can add an organization to their account if they know the
    organization name and associated discount code to be a member of them and
    have their discounts applied at checkout from their rentals. 

    updating customer information:
    works much the same way as creating a new customer, allows for changing of
    all but a system-generated identity number.

    displaying all rented cars:
    list cars will display all rented cars by the user, separatedinto sections by
    completed rentals, current rentals, and reservations. 

    cost calculation:
    cost calculations are done by multiplying the rate for a car (found in the
    vehi_type relation) by the number of days the rental persisted for.
    Miscelaneous charges are then added from the misc_charges relation (more on
    this later) as are insurance charges (which are in the rental relation)
    multiplied by the number of days the rental persists for. This number is
    then multiplied by the sum of all discounts both from the customer's
    membership in an organization as well as the discounts applied specifically
    to the current rental. Fuel charge is then done by taking the vehicle's max
    fuel capacity (because hurts always rents with a full tank). Fuel charge is
    not subject to discounts because, well, I don't really know but I feel like
    you're getting a discount on the car not the fuel when you're in an
    organization. The management view has a "revenue" option which shows total
    revenue for the rental car company based on cars which have been returned
    within a time period which the user is allowed to specify.

    miscelaneous charges:
    misc charges have 2 options which can be set. The charge can be a one-time
    charge or incurred daily via the one_time flag. The charge can also have a
    percentage charge component, which charges a percent of the rental rate for
    the car, or a flat charge, for which a dollar ammount is entered. A rental
    may have 0 or more miscelaneous charges.

    listing rentals:
    customers may list rentals belonging to them with "list rentals". This shows
    their rented cars grouped by completion status. The projected cost for these
    rentals assumes that cars are returned empty unless they have been returned
    with a different end fuel ammount by the user. The manager view can also
    view all rentals separated the same way. 

    adding cars:
    The manager has the capability to add a new car or car type to the database.
    In order for a car to be entered, its type must be known. The manager is
    allowed to create a single new type of car using "add car type", but this
    can be inefficient as there is a different type for every year. A bulk
    loader has been created with "bulk add cars" which allows the user to
    specify a year range to create car types with identical data for a span of
    years. When adding a car with "add car" the user is prompted to search
    through a set of parameters in order to narrow down a search for a specific
    vehicle type. (just try it you'll see how it works).

    removing cars:
    Vehicle types can't be removed, as they have no real bearing on the database
    whether they're in use or not, as they're abstract concepts. Cars can be
    removed via "remove car". Deleting a car cascades the foreign keys, so all
    rentals involving it should dissolve and it will no longer be shown as being
    at a location.

    a demo for you to try to get a scope of what this project does.
    key: your input is in [ ], output in {{ }}, explanations in ()

    {{enter customer or manager}}
    [customer]
    [test]      (logged in with random ID)
    [get date]
    {{prints out the rental system's date}}
    [reserve car]
        (here you'll enter data. only important thing is that you don't start
        before the current date, or end before start, or you'll end up having to
        reenter)
    [list rentals]
    {{
        completed
        no data
        ---------------------
        in progress
        no data
        ---------------------
        future
        (rental you just booked with estimated cost)
        ---------------------
    }}
    [set date]
        [(start date of rental)]
        [false]     (for this example you'll manually pick up the car)
    [pickup car]
    [list rentals]
        completed
        no data
        ---------------------
        in progress
        (rental you just booked with estimated cost)
        ---------------------
        future
        no data
        ---------------------
        (you'll see your rental shift to the "in progress" box

    [set date]
        (put it at or after the end date of your rental)
        [false] (you'll manually drop off the car)
    [drop off car]
        (here you'll be asked about fuel on return and then shown the cost)
    [list rentals]
        completed
        (rental you just completed with cost)
        ---------------------
        in progress
        no data
        ---------------------
        future
        no data
        ---------------------

---------------------------------------------------------------
    the above is a simple example. You'll see the full extent of what went into
    this project and the rental logic if you create 2 sequential rentals in
    which the dropoff location of the first is different from its pickup
    location and the same as the pickup location of the second. In this case,
    the same car is available for rent in both but only if the first rental is
    created before the second, because the system will calculate the dropoff
    location of the car. Next, you can pickup the first rental, and advance time
    to sometime within the second rental without returning the first rental.
    Trying to pickup the second rental will result in the program telling you
    that the car you're looking for is still out, and will cancel your
    reservation, encouraging you to book another. If you then return the first
    rental, you will be billed for a length based on the day you returned
    instead of the day you'd originally booked to return it, and the rental will
    reflect this in the database.

Data Generation:
    Data generation was done using a combination of python, some PLSQL, and a
    bit of Java. The java doesn't work all that well, but it's included anyways
    (called Generation.java in the source files because it is accessible from
    main just not in a visible way). All data generation and PLSQL (including
    triggers, procedures, and functions) are included in the sql folder at the
    root of this project. My data was pulled from a variety of sources. The
    vehicle type data was pulled from some tables on wikipedia which I did
    text-editing regex-replacement magic to put into a list of python tuples,
    names are gotten from double-nested for loops matching the most popular
    first names to the most popular last names. cars are generated in PLSQL once
    vehicle types are in. The relation with cars at locations is generated after
    that, and customers as members of organizations is generated in PLSQL as
    well using random numbers.

Triggers / functions:
    important triggers in this project are dropoff and pickup, which handle
    moving cars out of and into the "AT" relation when they are picked up and
    dropped off. The dropoff one caused problems with the way I bulk-returned
    and picked up cars using the "set date" functionality, so that method
    actually disables the trigger for a bit. (There's a code comment as to why I
    did it that way which probably explains better if you're REALLY interested).
    one function was written to calculate cost of a rental because that's a lot
    easier to do than writing it out in java with 1000000 queries and doing the
    arithmetic client-side.
