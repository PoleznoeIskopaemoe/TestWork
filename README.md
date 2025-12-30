Система позволяет регистрировать клиентов, управлять расписанием работы бассейна и бронировать время для посещения с учетом всех бизнес-ограничений.

Основные возможности:

Регистрация и управление клиентами

Гибкое расписание работы (обычные и праздничные дни)

Онлайн-бронирование слотом по часам

Автоматическая проверка всех бизнес-ограничений

Просмотр доступных и занятых слотов

Поиск записей по различным критериям

Поддержка транзакций и конкурентного доступа

Таблица 1: clients (Клиенты)
Назначение: Хранение информации о зарегистрированных клиентах бассейна.

id	SERIAL	PRIMARY KEY	Уникальный идентификатор клиента
name	VARCHAR(100)	NOT NULL	Полное имя клиента (ФИО)
phone	VARCHAR(20)	NOT NULL, UNIQUE	Номер телефона. Основной идентификатор клиента
email	VARCHAR(100)		Электронная почта для уведомлений
created_at	TIMESTAMP	DEFAULT CURRENT_TIMESTAMP	Дата и время регистрации
updated_at	TIMESTAMP	DEFAULT CURRENT_TIMESTAMP	Дата последнего обновления

Таблица 2: schedule_days (Расписание по дням)
Назначение: Хранение информации о графике работы бассейна для каждого конкретного дня.

id	SERIAL	PRIMARY KEY		Уникальный ID записи
date	DATE	NOT NULL, UNIQUE		Календарная дата
is_holiday	BOOLEAN		false	Флаг праздничного дня
opening_time	TIME	NOT NULL	08:00:00	Время открытия
closing_time	TIME	NOT NULL	22:00:00	Время закрытия
max_capacity	INTEGER		10	Макс. человек в час
created_at	TIMESTAMP	DEFAULT CURRENT_TIMESTAMP		Дата создания расписания


Таблица 3: appointments (Записи на посещение)
Назначение: Хранение информации о бронированиях клиентов на конкретные даты и время.

id	UUID	PRIMARY KEY	gen_random_uuid()	Уникальный ID записи (orderId)
client_id	INTEGER	NOT NULL, FOREIGN KEY		Ссылка на клиента
schedule_date	DATE	NOT NULL		Дата посещения
start_time	TIME	NOT NULL		Время начала (всегда на начало часа)
duration_hours	INTEGER		1	Продолжительность в часах
status	VARCHAR(20)		'active'	Статус: active, cancelled, completed
created_at	TIMESTAMP	DEFAULT CURRENT_TIMESTAMP		Дата создания брони


Таблица 4: time_slots (Слоты времени)
Назначение: Оптимизация контроля количества записей в каждый час. Используется составной первичный ключ.

schedule_date	DATE	PRIMARY KEY, FOREIGN KEY Дата слота
hour	TIME	PRIMARY KEY		Час слота (08:00, 09:00, ...)
booked_count	INTEGER		0	Количество занятых мест


Расписание и записи (/api/v0/pool/timetable)
GET    "/all?date=YYYY-MM-DD"	Занятые слоты на дату
GET	"/available?date=YYYY-MM-DD"	Доступные слоты на дату
POST	"/reserve"	Забронировать время
GET	"/cancel"	Отменить бронирование

Клиенты (/api/v0/pool/client)
GET	"/all"	Получить список всех клиентов
GET	"/get?id={id}"	Получить данные клиента по ID
POST	"/add"	Добавить нового клиента
POST	"/update"	Обновить данные клиента