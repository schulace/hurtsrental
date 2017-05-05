import os
import random
import string
vehi_type =[
('ford','compact','Ka',1996,2014),
('ford','compact','Fiesta',1976,2008),
('ford','compact','Focus',1998,2010),
('ford','compact','fusion',1992,2014),
('ford','compact','Taurus',1986,2009),
('ford','sports car','Fiesta ST',2013,2013),
('ford','sports car','Focus RS',2015,2015),
('ford','sports car','Mustang',1964,2014),
('ford','sports car','GT',2004,2016),
('ford','fullsize','B-Max',2012,2012),
('ford','fullsize','C-Max',2003,2010),
('ford','fullsize','S-Max',2006,2015),
('ford','fullsize','Galaxy',1996,2015),
('ford','SUV','Ecosport',2003,2013),
('ford','SUV','Kuga',2012,2012),
('ford','SUV','Flex',2009,2009),
('ford','SUV','Edge',2006,2015),
('ford','SUV','Explorer',1990,2011),
('ford','SUV','Expedition',1996,2007),
('ford','truck','Transit Connect',2002,2012),
('ford','truck','E-Series',1961,1992),
('ford','truck','Transit Courier',2014,2014),
('ford','truck','Transit Custom',2012,2012),
('ford','truck','Transit',1965,2013),
('ford','truck','Ranger',1971,2011),
('ford','truck','F-150',1948,2015),
('ford','truck','Super Duty',1999,2017),
('chevrolet','compact','Spark',1998,2015),
('chevrolet','compact','Sonic',2002,2011),
('chevrolet','compact','Bolt',2016,2016),
('chevrolet','compact','Volt',2010,2016),
('chevrolet','fullsize','Cruze',2008,2015),
('chevrolet','fullsize','Malibu',1964,2015),
('chevrolet','fullsize','Impala',1957,2013),
('chevrolet','sports car','SS',1978,2013),
('chevrolet','sports car','Camaro',1965,2016),
('chevrolet','sports car','Corvette',1966,2014),
('chevrolet','sports car','City Express',1953,2014),
('chevrolet','sports car','Trax',1996,2013),
('chevrolet','SUV','Equinox',2013,2017),
('chevrolet','SUV','Traverse',2004,2009),
('chevrolet','truck','Tahoe',2009,2015),
('chevrolet','truck','Suburban',1935,2012),
('chevrolet','truck','Colorado',1995,2013),
('chevrolet','truck','Silverado',2004,2014),
('isuzu','SUV','Axiom',2001,2001),
('Citroen','minicar','Deux Chevaux (2CV)',1948,1990),
('AMC','subcompact','Gremlin',1971,1978),
('AMC','compact','Hornet',1969,1970),
('AMC','compact','Pacer',1975,1979),
('Ford','fullsize','Edsel',1958,1960),
('Chevrolet','compact','Vega',1971,1977),
('Yugo','subcompact','Yugo',2007,2008),
('Trabant','subcompact','Trabant',1964,1990),
('Plymouth','sports car','Prowler',1999,2002),
('Pontiac','minivan','Aztek',2001,2005),
('Ford','subcompact','Pinto',1971,1980),
('Toyota','subcompact','Toyopet',1959,1961),
('Vaz','compact','Lada',1970,1989),
('Volkswagen','truck','Thing',1970,1980)
]
citystates = [
    ('New York','NY'),
    ('Los Angeles','CA'),
    ('Chicago','IL'),
    ('Houston','TX'),
    ('Philadelphia','PA'),
    ('Phoenix','AZ'),
    ('San Antonio','TX'),
    ('San Diego','CA'),
    ('Dallas','TX'),
    ('San Jose','CA'),
    ('Austin','TX'),
    ('Jacksonville','FL'),
    ('San Francisco','CA'),
    ('INpolis','IN'),
    ('Columbus','OH'),
    ('Fort Worth','TX'),
    ('Charlotte','NC'),
    ('Seattle','WA'),
    ('Denver','CO'),
    ('El Paso','TX'),
    ('Detroit','MI'),
    ('Washington','DC'),
    ('Boston','MA'),
    ('Memphis','TN'),
    ('Nashville','TN'),
    ('Portland','OR'),
    ('Oklahoma City','OK'),
    ('Las Vegas','NV'),
    ('Baltimore','MD'),
    ('Louisville','KY'),
    ('Milwaukee','WI'),
    ('Albuquerque','NM'),
    ('Tucson','AZ'),
    ('Fresno','CA'),
    ('Sacramento','CA'),
    ('Kansas City','MO'),
    ('Long Beach','CA'),
    ('Mesa','AZ'),
    ('Atlanta','GA'),
    ('Colorado Springs','CO'),
    ('Virginia Beach','VA'),
    ('Raleigh','NC'),
    ('Omaha','NE'),
    ('Miami','FL'),
    ('Oakland','CA'),
    ('Minneapolis','MN'),
    ('Tulsa','OK'),
    ('Wichita','KS'),
    ('New Orleans','LA'),
    ('Arlington','TX')
]

fnames = ['Emma','Olivia','Sophia','Ava','Isabella','Mia','Abigail','Emily','Charlotte','Harper','Madison','Amelia','Elizabeth','Sofia','Evelyn','Avery','Chloe','Ella','Grace','Victoria','Aubrey','Scarlett','Zoey','Addison','Lily','Lillian','Natalie','Hannah','Aria','Layla','Brooklyn','Alexa','Zoe','Penelope','Riley']
lnames = ['Smith','Johnson','Williams','Jones','Brown','Davis','Miller','Wilson','Moore','Taylor','Anderson','Thomas','Jackson','White','Harris','Martin','Thompson','Garcia','Martinez','Robinson','Clark','Rodriguez','Lewis','Lee','Walker','Hall','Allen','Young','Hernandez','King','Wright','Lopez','Hill','Scott']
streets = ['Second','Third','First','Fourth','Park','Fifth','Main','Sixth','Oak','Seventh','Pine','Maple','Cedar','Eighth','Elm','View','Washington','Ninth','Lake','Hill']
street_suffixes = ['drive','lane','ave','street','boulevard']
orgnames = ['Walmart','Exxon Mobil','Apple','Berkshire Hathaway','McKesson','UnitedHealth Group','CVS Health','General Motors','Ford Motor','ATT','General Electric','AmerisourceBergen','Verizon','Chevron','Costco','Fannie Mae','Kroger','Amazon.com','Walgreens Boots Alliance','HP','Cardinal Health','Express Scripts Holding','J.P. Morgan Chase','Boeing','Microsoft','Bank of America','Wells Fargo','Home Depot','Citigroup','Phillips 66','IBM','Valero Energy','Anthem','Procter   Gamble','State Farm','Alphabet','Comcast','Target','Johnson   Johnson','MetLife','Archer Daniels Midland','Marathon Petroleum','Freddie Mac','PepsiCo','United Technologies','Aetna','Lowes','UPS','AIG','Prudential Financial','Intel','Humana','Disney','Cisco Systems','Pfizer','Dow Chemical','Sysco','FedEx','Caterpillar','Lockheed Martin','New York Life Insurance','Coca-Cola','HCA Holdings','Ingram Micro','Energy Transfer Equity','Tyson Foods','American Airlines Group','Delta Air Lines','Nationwide','Johnson Controls','Best Buy','Merck','Liberty Mutual Insurance Group','Goldman Sachs Group','Honeywell International','Massachusetts Mutual Life Insurance','Oracle','Morgan Stanley','Cigna','United Continental Holdings','Allstate','TIAA','INTL FCStone','CHS','American Express','Gilead Sciences','Publix Super Markets','General Dynamics','TJX','ConocoPhillips','Nike','World Fuel Services','3M','Mondelez International','Exelon','Twenty-First Century Fox','Deere','Tesoro','Time Warner','Northwestern Mutual','DuPont']

with open(os.path.join(os.curdir, 'output.sql'), 'w') as outfile:
    outfile.write(""" alter procedure createat compile;
alter procedure creatememberof compile;
alter procedure createcars compile;
alter procedure removedata compile;
execute removedata;
--following inserts cover vehi_type, customer, organization, and location.
--ones to be created by oracle are at, car, member_of, misc_charge, org_discounts, rental
""")
    #vehicles
    for t in vehi_type:
        for year in range(t[3], t[4] + 1):
            rate = str(random.randrange(50, 150))
            outfile.write("insert into vehi_type(make, type, model, rate, year, fuel_capacity) VALUES  ('{}', '{}', '{}', {}, {}, {});\n".format(t[0], t[1], t[2], rate, str(year), random.randint(8,20)))
    #customers
    for f in fnames:
        for l in lnames:
            zip = "%05d" %random.randrange(0, 100000)
            houseNo = str(random.randrange(1,5000))
            citypair = citystates[random.randrange(0, len(citystates))]
            streetaddr = houseNo + " " + streets[random.randrange(0, len(streets))] + " " + street_suffixes[random.randrange(0,len(street_suffixes))]
            outfile.write("insert into CUSTOMER(FIRST_NAME, LAST_NAME, STREET_ADDR, CITY, STATE, LICENSE_NUMBER, LICENSE_ST, ZIP) values"
                          "('{}','{}','{}','{}','{}','{}','{}','{}');\n".format
                              (
                                    f,
                                    l,
                                    streetaddr,
                                    citypair[0],
                                    citypair[1],
                                    ''.join(random.choice(string.digits) for _ in range(0,9)),
                                    citystates[random.randrange(0,len(citystates)-1)][1],
                                    zip
                                )
                          )
    #organization
    for org in orgnames:
        outfile.write("insert into ORGANIZATION (ORG_NAME, CODE, DISCOUNT) VALUES ('{}', '{}', {});\n".format(org,''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(0,9)), random.random()/2))
    #locations
    for t in citystates:
        zip = "%05d" % random.randrange(0, 100000)
        houseNo = str(random.randrange(1, 5000))
        citypair = t
        streetaddr = houseNo + " " + streets[random.randrange(0, len(streets))] + " " + street_suffixes[
            random.randrange(0, len(street_suffixes))]
        outfile.write(
            "insert into LOCATION(street_addr, city, state, zip) values"
            "('{}','{}','{}','{}');\n".format
                (
                streetaddr,
                citypair[0],
                citypair[1],
                zip
            )
        )
    outfile.write(
"""execute createcars;
execute createat;
execute creatememberof;
--ones to be created by oracle are at, car, member_of, misc_charge, org_discounts, rental
""")

