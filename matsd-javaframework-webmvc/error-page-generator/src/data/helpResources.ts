import { Book, Github, type LucideIcon, MessageSquare, PlayCircle } from "lucide-react";

type ResourceType = "documentation" | "community & support" | "video" | "github";
type ResourceTypeConfig = {
  displayName: string;
  icon: LucideIcon;
  badgeClassName: string;
};

const RESOURCE_TYPE_CONFIGS: Record<ResourceType, ResourceTypeConfig> = {
  "documentation": {
    displayName: "Documentation",
    icon: Book,
    badgeClassName: "bg-blue-500/10 text-blue-500",
  },
  "community & support": {
    displayName: "Communauté & Support",
    icon: MessageSquare,
    badgeClassName: "bg-green-500/10 text-green-500",
  },
  "video": {
    displayName: "Vidéo",
    icon: PlayCircle,
    badgeClassName: "bg-red-500/10 text-red-500"
  },
  "github": {
    displayName: "GitHub",
    icon: Github,
    badgeClassName: "bg-purple-500/10 text-purple-500"
  }
};

export class ResourceItem {
  public readonly title: string;
  public readonly url:   string;
  public readonly description: string;
  public readonly type: ResourceType;

  constructor(title: string, url: string, description: string, type: ResourceType) {
    this.title = title;
    this.url   = url;
    this.description = description;
    this.type = type;
  }

  getDisplayType(): string {
    return RESOURCE_TYPE_CONFIGS[this.type].displayName;
  }

  getIcon(): LucideIcon {
    return RESOURCE_TYPE_CONFIGS[this.type].icon;
  }

  getBadgeClassName(): string {
    return RESOURCE_TYPE_CONFIGS[this.type].badgeClassName;
  }
}

export const helpResourcesData: ResourceItem[] = [
  new ResourceItem(
    "Documentation Java",
    "https://docs.oracle.com/en/java/",
    "Documentation officielle de Java par Oracle",
    "documentation"
  ),
  new ResourceItem(
    "Documentation Jakarta EE",
    "https://jakarta.ee/specifications/",
    "Spécifications officielles de Jakarta EE",
    "documentation"
  ),
  new ResourceItem(
    "Documentation Jakarta Servlet",
    "https://jakarta.ee/specifications/servlet/",
    "Documentation des servlets Jakarta",
    "documentation"
  ),
  new ResourceItem(
    "Java Tutorials",
    "https://docs.oracle.com/javase/tutorial/",
    "Tutoriels Java officiels par Oracle",
    "documentation"
  ),
  new ResourceItem(
    "Stack Overflow - Java",
    "https://stackoverflow.com/questions/tagged/java",
    "Questions et réponses de la communauté sur Java",
    "community & support"
  ),
  new ResourceItem(
    "Stack Overflow - Jakarta EE",
    "https://stackoverflow.com/questions/tagged/jakarta-ee",
    "Support communautaire pour Jakarta EE",
    "community & support"
  ),
  new ResourceItem(
    "Stack Overflow - Servlets",
    "https://stackoverflow.com/questions/tagged/servlet",
    "Support communautaire pour les servlets",
    "community & support"
  ),
  new ResourceItem(
    "matsd-javaframeworkproject",
    "https://github.com/Kevin-rm/matsd-javaframeworkproject",
    "Code source du framework hébergé sur GitHub",
    "github"
  ),
  new ResourceItem(
    "ticketing",
    "https://github.com/Kevin-rm/ticketing",
    "Application témoin servant de proof-of-concept",
    "github"
  ),
  new ResourceItem(
    "Tutoriel JEE",
    "https://www.youtube.com/watch?v=jevdND1NBVs&list=PLepmdz3mDsCpOcLtmn3r8UKfsjGSeDmma",
    "Série de vidéos sur JEE par Mathieu NEBRA",
    "video"
  ),
  new ResourceItem(
    "Java Full Course for Beginners",
    "https://www.youtube.com/watch?v=eIrMbAQSU34",
    "Tutoriel complet Java pour débutants par la page Programming with Mosh",
    "video"
  ),
  new ResourceItem(
    "Java Design Patterns",
    "https://java-design-patterns.com/",
    "Catalogue de design patterns en Java",
    "documentation"
  ),
  new ResourceItem(
    "Coding with John - YouTube",
    "https://www.youtube.com/c/CodingwithJohn",
    "Tutoriels Java modernes et de qualité par la page Coding with John",
    "video"
  )
];
