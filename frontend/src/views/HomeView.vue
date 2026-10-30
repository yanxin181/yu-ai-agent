<template>
  <div class="home-page">
    <header class="home-header page-container">
      <div class="brand-lockup">
        <span class="brand-mark"><AppIcon name="sparkles" size="sm" /></span>
        <span>YU / AI AGENT</span>
      </div>
      <span class="availability"><span class="status-dot" /> 系统在线</span>
    </header>

    <main class="home-main page-container">
      <section class="hero-copy">
        <div class="hero-kicker"><span /> INTELLIGENCE, WITH A HUMAN TOUCH</div>
        <h1>把复杂的问题，<em>交给你的 AI 伙伴。</em></h1>
        <p>从关系里的微妙情绪，到执行一件事的完整路径，选择一个智能应用，开始一段专注的对话。</p>
      </section>

      <section class="app-grid" aria-label="选择应用">
        <article class="app-card app-card--love">
          <div class="app-card__topline">
            <span class="app-card__index">01</span>
            <span class="app-card__tag">RELATIONSHIP</span>
          </div>
          <div class="app-card__icon"><AppIcon name="heart" size="xl" /></div>
          <div class="app-card__body">
            <h2>AI 恋爱大师</h2>
            <p>拆解关系里的困惑，读懂对方的情绪，也找到更舒服的表达方式。</p>
          </div>
          <RouterLink class="app-card__action" to="/love">
            进入对话 <AppIcon name="arrow-right" size="sm" />
          </RouterLink>
        </article>

        <article class="app-card app-card--manus">
          <div class="app-card__topline">
            <span class="app-card__index">02</span>
            <span class="app-card__tag">EXECUTION PARTNER</span>
          </div>
          <div class="app-card__icon"><AppIcon name="brain" size="xl" /></div>
          <div class="app-card__body">
            <h2>AI 超级智能体</h2>
            <p>把目标交给一个可以思考、搜索、规划并执行的智能工作伙伴。</p>
          </div>
          <RouterLink class="app-card__action" to="/manus">
            开始协作 <AppIcon name="arrow-right" size="sm" />
          </RouterLink>
        </article>
      </section>

      <section class="home-footnote">
        <span class="home-footnote__line" />
        <span>Choose a space to begin</span>
        <span class="home-footnote__line" />
      </section>
    </main>

    <footer class="home-footer page-container">
      <span>YU AI AGENT · 2026</span>
      <span>Built for thoughtful work</span>
    </footer>
  </div>
</template>

<script setup>
import AppIcon from '../components/AppIcon.vue'
</script>
