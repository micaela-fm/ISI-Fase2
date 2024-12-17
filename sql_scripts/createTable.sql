create table typeof(
	reference char(10) primary key, 
	nodays integer check (nodays > 0), 
	price numeric(4, 2) check (price > 0)
);

create table person(
	id serial primary key, 
	email varchar(40) unique check (email like '%@%'), 
	taxnumber integer unique, 
	name varchar(50)
); 

create table employee(
	number serial unique, 
	person integer primary key, 
	foreign key (person) references person(id) on delete cascade 
); 

create table client(
	person integer primary key, 
	dtregister timestamp default current_timestamp, 
	foreign key (person) references person(id) on delete cascade
);

create table card(
	id serial primary key, 
	credit numeric(4, 2) check (credit > 0), 
	typeof char(10), 
	client integer, 
	foreign key (typeof) references typeof(reference) on delete set null,
	foreign key (client) references client(person) on delete cascade
); 

create table topup(
	dttopup timestamp default current_timestamp, 
	card integer, 
	value numeric(4, 2) check (value > 0), 
	primary key (dttopup, card), 
	foreign key (card) references card(id) on delete cascade 
);

create table scootermodel(
	number serial primary key, 
	designation varchar(30), 
	autonomy integer check (autonomy > 0)
);

create table scooter(
	id serial primary key, 
	weight numeric(4, 2) check (weight > 0), 
	maxvelocity numeric(4, 2) check (maxvelocity > 0), 
	battery integer check (battery > 0), 
	model integer, 
	foreign key (model) references scootermodel(number) on delete cascade
);

create table station(
	id serial primary key, 
	latitude numeric(6, 4) check (latitude between -90 and 90), 
	longitude numeric(6, 4) check (longitude between -180 and 180)
); 

create table travel(
	dtinitial timestamp default current_timestamp, 
	comment varchar(100), -- se não houver evaluation, é nulo. como garantir isso?
	evaluation integer check (evaluation between 1 and 5), 
	dtfinal timestamp check (dtfinal > dtinitial), 
	client integer, 
	scooter integer, 
	stinitial integer, 
	stfinal integer, 
	primary key (dtinitial, client), 
	unique (dtinitial, scooter),
	foreign key (client) references client(person) on delete cascade, 
	foreign key (scooter) references scooter(id) on delete set null, 
	foreign key (stinitial) references station(id) on delete set null, 
	foreign key (stfinal) references station(id) on delete set null 
);

create table dock(
	number serial, 
	station integer, 
	state varchar(30) default 'under maintenance' check (state in ('free', 'occupy', 'under maintenance')), 
	scooter integer, -- se for null state não pode ser 'occupy'. como implementar isto?
	primary key (number, station), 
	foreign key (scooter) references scooter(id) on delete cascade
); 

create table replacementorder(
	dtorder timestamp default current_timestamp, 
	dtreplacement timestamp check (dtreplacement > dtorder), 
	roccupation integer check (roccupation between 0 and 100), 
	station integer, 
	primary key (dtorder, station),
	foreign key (station) references station(id) on delete cascade
);

create table replacement(
	number serial, 
	dtreplacement timestamp default current_timestamp check (dtreplacement > dtreporder), 
	action char(8) check (action in ('inplace','remove')), 
	dtreporder timestamp, 
	repstation integer, 
	employee integer, 
	primary key (number, dtreporder, repstation), 
	foreign key (dtreporder, repstation) references replacementorder(dtorder, station) on delete cascade, 
	foreign key (employee) references employee(person) on delete set null 
);

create table servicecost(
	unlock numeric(3, 2), 
	usable numeric(3, 2)
);
