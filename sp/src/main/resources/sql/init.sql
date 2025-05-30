create table if not exists locations (
    id int unsigned primary key auto_increment,
    city varchar(50),
    county varchar(50)
    );
truncate table locations;

INSERT INTO locations VALUES
                          (null, 'Bristol', 'Bristol'),
                          (null, 'Manchester', 'Greater Manchester'),
                          (null, 'Liverpool', 'Merseyside'),
                          (null, 'Leeds', 'West Yorkshire'),
                          (null, 'Sheffield', 'South Yorkshire'),
                          (null, 'Birmingham', 'West Midlands'),
                          (null, 'Nottingham', 'Nottinghamshire'),
                          (null, 'Leicester', 'Leicestershire'),
                          (null, 'Oxford', 'Oxfordshire'),
                          (null, 'Cambridge', 'Cambridgeshire'),
                          (null, 'York', 'North Yorkshire'),
                          (null, 'Newcastle', 'Tyne and Wear'),
                          (null, 'Exeter', 'Devon'),
                          (null, 'Plymouth', 'Devon'),
                          (null, 'Norwich', 'Norfolk');

create index city_idx on locations (city);
create index county_idx on locations (county);
create index city_county_coidx on locations (city, county);