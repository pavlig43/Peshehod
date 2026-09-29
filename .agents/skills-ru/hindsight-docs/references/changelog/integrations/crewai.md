---
hide_table_of_contents: true
---

import PageHero from '@site/src/components/PageHero';

<PageHero title="CrewAI — история изменений" subtitle="hindsight-crewai — долговременная память для агентов CrewAI." />

← CrewAI — интеграция

## [0.4.20](https://github.com/vectorize-io/hindsight/tree/integrations/crewai/v0.4.20)

**Новые возможности**

- Добавлена интеграция CrewAI: группы агентов могут хранить долговременную память в Hindsight. ([`41db2960`](https://github.com/vectorize-io/hindsight/commit/41db2960))

**Улучшения**

- Все HTTP-запросы теперь содержат заголовок User-Agent для лучшей совместимости с внешними сервисами и отслеживания запросов. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))
- Улучшена проверка типов в Python: все пакеты теперь содержат сведения о типах по PEP 561. ([`d054b884`](https://github.com/vectorize-io/hindsight/commit/d054b884))
- Устранены известные уязвимости в зависимостях. ([`300d089b`](https://github.com/vectorize-io/hindsight/commit/300d089b))
