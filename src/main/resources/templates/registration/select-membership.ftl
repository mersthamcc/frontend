<#import "../base.ftl" as layout>
<#import "../components.ftl" as components />

<@layout.mainLayout>
    <@components.panel title="membership.register" type="info">
            <@components.membershipCategories categories=categories _csrf=_csrf />
    </@components.panel>
</@layout.mainLayout>