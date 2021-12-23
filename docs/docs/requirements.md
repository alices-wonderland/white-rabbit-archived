---
sidebar_position: 2
slug: requirements
---

# Requirement Analysis

## Domain Knowledge

### Accounts

The account is the category of financial behaviors. There are **five** first-class accounts:

- Assets
- Liabilities
- Income
- Expenses
- Equity

With important identity:

$$
assets = liabilities + equity + (income - expenses)
$$

The key of accounting is how to make the identity **zero-out**.

#### Accounts: Basic Properties

- `type`: Which of the first-class accounts belonged to
- `name`: A hierarchical structure, representing as a string array
- `unit`: The commodity unit of this account
- `inventoryRecords`: The inventory records of the commodity
- `bookingOperation`: FIFO, AVERAGE

**Inventory Record:**

- `timestamp`: When this commodity checked in/out
- `amount`: The amount of this commodity
- `unitPrice`: Optional, the unit price of this commodity

### FinRecord: Financial Record

**FinRecord** is the smallest item in one financial action.

#### FinRecord: Basic Properties

- `id`: The unique ID for one FinRecord
- `author`: Who add this record
- `isDeleted`: Is this record deleted
- `happenedAt`: When this action happened
- `createdAt`: When this record added
- `name`: The name of this action
- `isContingent`: If you have any questions about this action
- `items`: Record items. At least two members. Should be zero-out
- `tags`: Tags of this record
- `basicUnit`: Basic unit, always be a concurrency type
- `unitMapping`: The mapping relationships between the `basicUnit` and others
  - All the units mentioned in a FinRecord should be mapped here (unless the basicUnit)
- `updateHistories`: The updating histories on this record
- `note`: The note to this record

**FinRecord Item:**

- `account`: The account associated
- `amount`: The amount of this action
- `unit`: The unit of this action, always be a concurrency unit but can be others
- `unitPrice`: Optional, the unit price of this commodity
- `note`: The note to this item

**FinRecord History:**

- `timestamp`: When this update happened
- `author`: Who updated this
- `fields`: A list for which fields updated

**Update Result:**

- `<field-name>`: Object key is the field name, value is: `{ type: "ADDED | UPDATED | REMOVED", data: <primitive data | another update result> }`

## User Cases

```plantuml
@startuml
left to right direction

User <|-- Admin

package journals {
  User -> (Can create a Journal)

  (Can access a readable Journal) as AccessJournal
  User -> AccessJournal
  note right of AccessJournal
  A readable Journal:
  * User is global Admin
  * OR - User is Role_User in the Journal
  * OR - User is Role_Admin in the Journal

  ReadOnly/ReadWrite List can contain:
  * a user
  * OR - a group
  end note

  User -> (Can update/delete a Journal info created by self)

  Admin -> (Can read/update/delete all Journals)
}

package finRecords {
  User -> (Can create a FinRecord)

  (Can add a FinRecord to a writeable Journal) as AddFinRecord
  User -> AddFinRecord
  note right of AddFinRecord
  A writeable Journal:
  * User is global Admin
  * OR - User is Role_Admin in the Journal
  end note

  User -> (Can update/delete the FinRecord in a writeable Journal)
  User -> (Can get the history records of a FinRecord)

  User -> (Can add assertions into a Journal)

  Admin -> (Can read/update/delete all FinRecords)

  package webmode {
    User -> ([Web Mode] Can do operations with other users)
  }
}

package statements {
  User -> (Can get financial statements in a readable Journal)
}

package integrations {
  User -> (Can import/export data via BeanCount/Ledger)
}
@enduml
```
